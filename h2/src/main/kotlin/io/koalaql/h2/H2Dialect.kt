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
import io.koalaql.dsl.value
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
                is RelvarColumn<*> -> sql.addIdentifier(expr.symbol)
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

                comma.forEach(table.columns) {
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
            sql.addResolved(scope.resolve(name))
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

        fun compileQuery(query: BuiltSubquery, outerSelect: List<SelectedExpr<*>>? = null): Boolean {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return when (query) {
                is BuiltSelectQuery -> {
                    compilation.compileSelect(query)
                    true
                }
                is BuiltValuesQuery -> compilation.compileValues(query)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(subquery, null)
            }
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) =
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
                is Relvar -> {
                    sql.addIdentifier(baseRelation.tableName)
                    null
                }
                is Subquery -> {
                    val innerScope = scope.innerScope()

                    baseRelation.of.populateScope(innerScope)

                    sql.parenthesize {
                        Compilation(
                            innerScope,
                            sql = sql
                        ).compileQuery(baseRelation.of)
                    }

                    if (baseRelation.of is BuiltValuesQuery) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
                }
                is Values -> {
                    sql.parenthesize {
                        compileValues(BuiltValuesQuery(baseRelation))
                    }
                    baseRelation.columns
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

        fun compileSetOperation(
            outerSelect: List<SelectedExpr<*>>,
            operation: BuiltSetOperation
        ) {
            sql.addSql("\n")
            sql.addSql(operation.type.sql)
            if (operation.distinctness == Distinctness.ALL) sql.addSql(" ALL")
            sql.addSql("\n")

            val selectQuery = operation.body.toSelectQuery(outerSelect)

            Compilation(
                scope.innerScope().also {
                    selectQuery.populateScope(it)
                },
                sql
            ).compileSelect(selectQuery)
        }

        fun compileWiths(withType: WithType, withs: List<BuiltWith>) {
            sql
                .prefix(
                    when (withType) {
                        WithType.RECURSIVE -> "WITH RECURSIVE "
                        WithType.NOT_RECURSIVE -> "WITH "
                    },
                    "\n, "
                )
                .forEach(withs) {
                    val innerScope = scope.innerScope()

                    it.query.populateScope(innerScope)

                    sql.addSql(scope[it.cte])

                    when (val query = it.query) {
                        is BuiltValuesQuery -> compileRelabels(query.columns)
                        else -> { }
                    }

                    sql.addSql(" AS (")

                    Compilation(
                        scope = innerScope,
                        sql = sql
                    ).compileQuery(it.query)

                    sql.addSql(")")
                }
        }

        fun compileWindows(windows: List<LabeledWindow>) {
            sql.prefix("\nWINDOW ", "\n, ").forEach(windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(BuiltWindow.from(it.window))
                sql.addSql(")")
            }
        }

        fun compileSelect(select: BuiltSelectQuery) {
            val withs = select.body.withs

            compileWiths(select.body.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            sql.selectClause(select.selected) {
                compileExpr(it.expr, false)
                sql.addSql(" ")
                sql.addIdentifier(scope.nameOf(it.name))
            }

            if (select.body.relation.relation != EmptyRelation) sql.addSql("\nFROM ")

            sql.compileQueryBody(
                select.body,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) }
            )

            select.body.setOperations.forEach {
                compileSetOperation(select.selected, it)
            }

            if (select.body.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.body.orderBy)

            select.body.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(value(it))
            }

            if (select.body.offset != 0) {
                check(select.body.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(select.body.offset))
            }

            select.body.locking?.let { locking ->
                when (locking) {
                    LockMode.SHARE -> sql.addSql("\nFOR SHARE")
                    LockMode.UPDATE -> sql.addSql("\nFOR UPDATE")
                }
            }
        }

        fun compileValues(query: BuiltValuesQuery): Boolean {
            return sql.compileValues(query, compileExpr = { compileExpr(it, false) })
        }

        fun compileInsert(insert: BuiltInsert): Boolean {
            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.compileInsertLine(insert)

            sql.addSql("\n")

            val relvar = insert.unwrapTable()

            val nonEmpty = compileQuery(insert.query)

            sql.compileOnConflict(insert.onConflict) { assignments ->
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

            return nonEmpty
        }

        fun compileUpdate(update: BuiltUpdate) {
            val query = update.query

            compileWiths(query.withType, query.withs)

            if (query.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("UPDATE ")

            compileRelation(update.query.relation)

            sql.addSql("\nSET ")

            val updatePrefix = sql.prefix("", ", ")

            check(query.joins.isEmpty()) {
                "H2 does not support JOIN in update"
            }

            update.assignments
                .forEach {
                    updatePrefix.next {
                        compileExpr(it.reference, false)
                        sql.addSql(" = ")
                        compileExpr(it.expr)
                    }
                }

            query.where?.let {
                sql.addSql("\nWHERE ")
                compileExpr(it, false)
            }
        }

        fun compileDelete(select: BuiltDelete) {
            val withs = select.query.withs

            compileWiths(select.query.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("\nDELETE FROM ")

            sql.compileQueryBody(
                select.query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) },
                compileJoins = { error("can't delete a join") },
                compileGroupBy = { error("can't group by in a delete") },
                compileHaving = { error("can't having in a delete") }
            )

            check(select.query.setOperations.isEmpty())

            if (select.query.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.query.orderBy)

            select.query.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(value(it))
            }

            if (select.query.offset != 0) {
                check(select.query.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(select.query.offset))
            }

            check(select.query.locking == null)
        }
    }

    override fun compile(dml: BuiltDml): SqlText? {
        val registry = NameRegistry()
        val scope = Scope(registry)

        val compilation = Compilation(
            scope = scope
        )

        dml.populateScope(scope)

        val nonEmpty = when (dml) {
            is BuiltSelectQuery -> {
                compilation.compileSelect(dml)
                true
            }
            is BuiltValuesQuery -> compilation.compileValues(dml)
            is BuiltInsert -> compilation.compileInsert(dml)
            is BuiltUpdate -> {
                compilation.compileUpdate(dml)
                true
            }
            is BuiltDelete -> {
                compilation.compileDelete(dml)
                true
            }
        }

        return if (nonEmpty) {
            compilation.sql.toSql()
        } else {
            null
        }
    }
}