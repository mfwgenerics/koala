package io.koalaql.h2

import io.koalaql.ddl.IndexType
import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.UnmappedDataType
import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.ColumnDefaultExpr
import io.koalaql.ddl.built.ColumnDefaultValue
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.*
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.*
import io.koalaql.window.*
import io.koalaql.window.built.BuiltWindow
import kotlin.reflect.KClass

class H2Dialect(
    private val compatibilityMode: H2CompatibilityMode? = null
): SqlDialect {
    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<SqlText>()

        change.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            Compilation(
                Scope(NameRegistry()),
                sql
            ).compileCreateTable(sql, table)

            results.add(sql.toSql())
        }

        return results
    }

    private inner class Compilation(
        val scope: Scope,
        override val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)
    ): ExpressionCompiler {
        private fun compileDefaultExpr(expr: Expr<*>) {
            when (expr) {
                is Literal -> sql.addLiteral(expr)
                is Column<*> -> sql.addIdentifier(expr.symbol)
                else -> compileExpr(expr)
            }
        }

        private fun compileDataType(type: UnmappedDataType<*>) {
            sql.addSql(type.defaultRawSql())
        }

        private fun compileColumnDef(column: TableColumn<*>) {
            val def = column.builtDef

            sql.addIdentifier(column.symbol)
            sql.addSql(" ")
            compileDataType(def.columnType.dataType)

            if (def.autoIncrement) sql.addSql(" AUTO_INCREMENT")
            if (def.notNull) sql.addSql(" NOT NULL")

            def.default?.let { default ->
                @Suppress("unchecked_cast")
                val finalExpr = when (default) {
                    is ColumnDefaultExpr -> default.expr
                    is ColumnDefaultValue -> Literal(
                        def.columnType.type as KClass<Any>,
                        default.value
                    )
                }

                sql.addSql(" DEFAULT ")
                compileDefaultExpr(finalExpr)
            }
        }

        private fun compileIndexDef(name: String, def: BuiltIndexDef) {
            sql.addSql(when (def.type) {
                IndexType.PRIMARY -> "PRIMARY KEY"
                IndexType.UNIQUE -> "UNIQUE KEY"
                IndexType.INDEX -> "INDEX"
            })

            sql.addSql(" ")
            sql.addIdentifier(name)
            sql.parenthesize {
                sql.prefix("", ", ").forEach(def.keys.keys) { key ->
                    compileDefaultExpr(key)
                }
            }
        }

        fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
            sql.addSql("CREATE TABLE IF NOT EXISTS ")

            sql.addIdentifier(table.tableName)
            sql.parenthesize {
                val comma = sql.prefix("\n", ",\n")

                comma.forEach(table.columns.includingUnused()) {
                    compileColumnDef(it)
                }

                table.primaryKey?.let { pk ->
                    comma.next {
                        sql.addSql("CONSTRAINT ")
                        sql.addIdentifier(pk.name)
                        sql.addSql(" PRIMARY KEY (")
                        sql.prefix("", ", ").forEach(pk.def.keys.keys) {
                            when (it) {
                                is TableColumn<*> -> sql.addIdentifier(it.symbol)
                                else -> error("expression keys unsupported")
                            }
                        }
                        sql.addSql(")")
                    }
                }

                table.indexes.forEach { index ->
                    if (index.def.type != IndexType.INDEX) {
                        comma.next {
                            compileIndexDef(index.name, index.def)
                        }
                    }
                }

                sql.addSql("\n")
            }
        }

        fun compileReference(name: Reference<*>) {
            sql.withResult(scope.resolve(name)) {
                sql.addResolved(it)
            }
        }

        override fun excluded(reference: Reference<*>) {
            when (compatibilityMode) {
                H2CompatibilityMode.MYSQL -> {
                    sql.addSql("VALUES")
                    sql.parenthesize {
                        compileReference(reference)
                    }
                }
                null -> sql.addError("Excluded[] is not supported by this dialect")
            }
        }

        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) =
            compileReference(value)

        fun compileOrderBy(ordinals: List<Ordinal<*>>) =
            sql.compileOrderBy(ordinals) { compileExpr(it, false) }

        fun compileAggregatable(aggregatable: BuiltAggregatable) {
            if (aggregatable.distinct == Distinctness.DISTINCT) sql.addSql("DISTINCT ")

            compileExpr(aggregatable.expr, false)

            if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
        }

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) =
            compileAggregatable(aggregatable)

        fun compileRangeMarker(direction: String, marker: FrameRangeMarker<*>) {
            when (marker) {
                CurrentRow -> sql.addSql("CURRENT ROW")
                is Following<*> -> {
                    compileExpr(marker.offset)
                }
                is Preceding<*> -> {
                    compileExpr(marker.offset)
                }
                Unbounded -> sql.addSql("UNBOUNDED $direction")
            }
        }

        fun compileWindow(window: BuiltWindow) {
            val partitionedBy = window.partitions.partitions
            val orderBy = window.partitions.orderBy

            val prefix = sql.prefix("", " ")

            window.partitions.from?.let {
                prefix.next {
                    sql.addSql(scope.nameOf(it))
                }
            }

            if (partitionedBy.isNotEmpty()) prefix.next {
                sql.prefix("PARTITION BY ", ", ").forEach(partitionedBy) {
                    compileExpr(it, false)
                }
            }

            if (orderBy.isNotEmpty()) prefix.next {
                compileOrderBy(orderBy)
            }

            window.type?.let { windowType ->
                prefix.next {
                    sql.addSql(windowType.sql)
                    sql.addSql(" ")

                    val until = window.until

                    if (until == null) {
                        compileRangeMarker("PRECEDING", window.from)
                    } else {
                        sql.addSql("BETWEEN ")
                        compileRangeMarker("PRECEDING", window.from)
                        sql.addSql(" AND ")
                        compileRangeMarker("FOLLOWING", until)
                    }
                }
            }
        }

        override fun window(window: BuiltWindow) {
            compileWindow(window)
        }

        fun compileCastDataType(type: UnmappedDataType<*>) {
            sql.addSql(type.defaultRawSql())
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) =
            compileCastDataType(to)

        inline fun <T> scopedIn(query: PopulatesScope, block: Compilation.() -> T): T {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return compilation.block()
        }

        // TODO remove this after WITH changes
        private inline fun <T> scopedCtesIn(query: BuiltQuery, block: Compilation.() -> T): T {
            val innerScope = scope.innerScope()

            query.populateCtes(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return compilation.block()
        }

        fun compileQuery(query: BuiltUnionOperandQuery): Boolean {
            return when (query) {
                is BuiltSelectQuery -> {
                    scopedIn(query) {
                        compileSelect(query)
                    }
                    true
                }
                is BuiltValuesQuery -> compileValues(query)
            }
        }

        fun compileQuery(query: BuiltQuery): Boolean {
            return compileFullQuery(query)
        }

        fun compileSubqueryExpr(subquery: BuiltQuery) {
            sql.parenthesize {
                compileQuery(subquery)
            }
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltQuery) =
            compileSubqueryExpr(subquery)

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            sql.compileExpr(expr, emitParens, this)
        }

        fun compileRelabels(labels: List<Reference<*>>) {
            sql.parenthesize {
                sql.prefix("", ", ").forEach(labels) {
                    sql.addIdentifier(scope.nameOf(it))
                }
            }
        }

        fun compileRelation(relation: BuiltRelation) {
            val explicitLabels = when (val baseRelation = relation.relation) {
                is TableRelation -> {
                    sql.addIdentifier(baseRelation.tableName)
                    null
                }
                is Subquery -> {
                    val innerScope = scope.innerScope()

                    baseRelation.of.populateScope(innerScope)

                    sql.parenthesize {
                        compileQuery(baseRelation.of)
                    }

                    if (baseRelation.of.columnsUnnamed()) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
                }
                is Cte -> {
                    sql.addSql(scope[baseRelation])

                    if (relation.computedAlias.identifier == baseRelation.identifier) return

                    null
                }
                is EmptyRelation -> return
            }

            sql.addSql(" ")
            sql.addSql(scope[relation.computedAlias])

            explicitLabels?.let { labels ->
                compileRelabels(labels)
            }
        }

        fun compileWiths(withable: BuiltWithable) = sql.compileWiths(
            withable,
            compileCte = { sql.addSql(scope[it]) },
            compileRelabels = { compileRelabels(it) },
            compileQuery = { compileQuery(it) }
        )

        fun compileWindows(windows: List<LabeledWindow>) {
            sql.prefix("\nWINDOW ", "\n, ").forEach(windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(BuiltWindow.from(it.window))
                sql.addSql(")")
            }
        }

        fun compileFullQuery(query: BuiltQuery): Boolean {
            return scopedCtesIn(query) {
                sql.compileFullQuery(
                    query = query,
                    compileWiths = { compileWiths(it) },
                    compileSubquery = { compileQuery(it) },
                    compileOrderBy = {
                        scopedIn(query) {
                            compileOrderBy(it)
                        }
                    }
                )
            }
        }

        fun compileSelect(select: BuiltSelectQuery) {
            sql.selectClause(select, scope) { compileExpr(it, false) }

            if (select.body.relation.relation != EmptyRelation) sql.addSql("\nFROM ")

            sql.compileQueryBody(
                select.body,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) }
            )
        }

        fun compileValues(query: BuiltValuesQuery): Boolean {
            return sql.compileValues(query, compileExpr = { compileExpr(it, false) })
        }

        fun compileInsert(insert: BuiltInsert): Boolean = sql.compileInsert(
            insert,
            compileInsertLine = { sql.compileInsertLine(insert) },
            compileQuery = { compileQuery(it) },
            compileOnConflict = {
                val relvar = insert.unwrapTable()

                sql.compileOnConflict(it) { assignments ->
                    val innerScope = scope.innerScope()

                    relvar.columns.forEach {
                        innerScope.internal(it, it.symbol, null)
                    }

                    val updateCtx = Compilation(innerScope, sql)

                    sql.prefix(" ", "\n,").forEach(assignments) {
                        updateCtx.compileExpr(it.reference)
                        sql.addSql(" = ")
                        updateCtx.compileExpr(it.expr)
                    }
                }
            }
        )

        fun compileUpdate(update: BuiltUpdate) = sql.compileUpdate(update,
            compileWiths = { compileWiths(it) },
            compileRelation = { compileRelation(it) },
            compileAssignment = {
                compileExpr(it.reference, false)
                sql.addSql(" = ")
                compileExpr(it.expr)
            },
            compileExpr = { compileExpr(it, false) }
        )

        fun compileDelete(delete: BuiltDelete) = sql.compileDelete(delete,
            compileWiths = { compileWiths(it) },
            compileQueryBody = { query ->
                sql.compileQueryBody(
                    query,
                    compileExpr = { compileExpr(it, false) },
                    compileRelation = { compileRelation(it) },
                    compileWindows = { windows -> compileWindows(windows) }
                )
            }
        )
    }

    override fun compile(dml: BuiltDml): SqlText? {
        return with(Compilation(scope = Scope(NameRegistry { "C${it + 1}" }))) {
            val nonEmpty = when (dml) {
                is BuiltQuery -> compileQuery(dml)
                is BuiltInsert -> scopedIn(dml) { compileInsert(dml) }
                is BuiltUpdate -> scopedIn(dml) { compileUpdate(dml) }
                is BuiltDelete -> {
                    scopedIn(dml) { compileDelete(dml) }
                    true
                }
            }

            if (nonEmpty) {
                sql.toSql()
            } else {
                null
            }
        }
    }
}