package io.koalaql.postgres

import io.koalaql.ddl.*
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

private fun UnmappedDataType<*>.toRawSql(): String = when (this) {
    DOUBLE -> "DOUBLE PRECISION"
    else -> defaultRawSql()
}

class PostgresDialect: SqlDialect {
    private fun compileDefaultExpr(sql: SqlTextBuilder, expr: Expr<*>) {
        when (expr) {
            is Literal -> sql.addLiteral(expr)
            is Column<*> -> sql.addIdentifier(expr.symbol)
            else -> error("not implemented")
        }
    }

    private fun compileDataType(sql: SqlTextBuilder, type: UnmappedDataType<*>) {
        sql.addSql(type.toRawSql())
    }

    private fun compileSerialType(sql: SqlTextBuilder, type: UnmappedDataType<*>) {
        when (type) {
            SMALLINT -> sql.addSql("SMALLSERIAL")
            INTEGER -> sql.addSql("SERIAL")
            BIGINT -> sql.addSql("BIGSERIAL")
            else -> sql.addError("no serial type corresponds to $type")
        }
    }

    private fun compileIndexDef(sql: SqlTextBuilder, name: String, def: BuiltIndexDef) {
        sql.addSql("CONSTRAINT ")

        sql.addIdentifier(name)

        sql.addSql(when (def.type) {
            IndexType.PRIMARY -> " PRIMARY KEY"
            IndexType.UNIQUE -> " UNIQUE"
            IndexType.INDEX -> " INDEX"
        })

        sql.addSql(" ")
        sql.parenthesize {
            sql.prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDefaultExpr(sql, key)
            }
        }
    }

    private fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
        sql.addSql("CREATE TABLE IF NOT EXISTS ")

        sql.addIdentifier(table.tableName)
        sql.parenthesize {
            val comma = sql.prefix("\n", ",\n")

            comma.forEach(table.columns.includingUnused()) {
                val def = it.builtDef

                sql.addIdentifier(it.symbol)
                sql.addSql(" ")

                if (def.autoIncrement) {
                    compileSerialType(sql, def.columnType.dataType)
                } else {
                    compileDataType(sql, def.columnType.dataType)
                }

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
                    compileDefaultExpr(sql, finalExpr)
                }
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
                comma.next {
                    compileIndexDef(sql, index.name, index.def)
                }
            }

            sql.addSql("\n")
        }
    }

    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<SqlText>()

        change.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            compileCreateTable(sql, table)

            results.add(sql.toSql())
        }

        return results
    }

    private class Compilation(
        val scope: Scope,
        override val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)
    ): ExpressionCompiler {
        fun compileReference(name: Reference<*>) {
            sql.withResult(scope.resolve(name)) {
                sql.addResolved(it)
            }
        }

        fun compileOrderBy(ordinals: List<Ordinal<*>>) {
            sql.compileOrderBy(ordinals) {
                compileExpr(it, false)
            }
        }

        fun compileAggregatable(aggregatable: BuiltAggregatable) {
            if (aggregatable.distinct == Distinctness.DISTINCT) sql.addSql("DISTINCT ")

            compileExpr(aggregatable.expr, false)

            if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
        }

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

        fun compileCastDataType(type: UnmappedDataType<*>) {
            sql.addSql(type.toRawSql())
        }

        fun compileQuery(outerSelect: List<SelectedExpr<*>>, query: BuiltSubquery): Boolean {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return when (query) {
                is BuiltSelectQuery -> {
                    compilation.compileSelect(query)
                    return true
                }
                is BuiltValuesQuery -> compilation.compileValues(query)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(emptyList(), subquery)
            }
        }

        fun compileSetLhs(expr: Reference<*>) {
            sql.withResult(scope.resolve(expr)) {
                sql.addIdentifier(it.innerName)
            }
        }

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            sql.compileExpr(expr, emitParens, this)
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
                        Compilation(
                            innerScope,
                            sql = sql
                        ).compileQuery(emptyList(), baseRelation.of)
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
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(labels) {
                        sql.addSql(scope.nameOf(it))
                    }
                }
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

        fun compileRelabels(labels: List<Reference<*>>) {
            sql.parenthesize {
                sql.prefix("", ", ").forEach(labels) {
                    sql.addIdentifier(scope.nameOf(it))
                }
            }
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
                    ).compileQuery(emptyList(), it.query)

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
                compileWindows = { windows -> compileWindows(windows) },
                compileSetOperation = { compileSetOperation(select.selected, it) },
                selectedLabels = select.selected.asSequence().map { it.name }.toSet(),
                compileOrderByLabel = { sql.addIdentifier(scope.nameOf(it)) }
            )
        }

        fun compileValues(query: BuiltValuesQuery): Boolean {
            return sql.compileValues(query, compileExpr = { compileExpr(it, false) })
        }

        fun compileInsert(insert: BuiltInsert): Boolean {
            val relvar = insert.unwrapTable()

            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.compileInsertLine(insert) {
                sql.addIdentifier(relvar.tableName)
                sql.addSql(" AS ")
                sql.addSql(scope[insert.relation.computedAlias])
            }

            sql.addSql("\n")

            val nonEmpty = compileQuery(emptyList(), insert.query)

            sql.compileOnConflict(insert.onConflict) { assignments ->
                val innerScope = scope.innerScope()

                relvar.columns.forEach {
                    innerScope.internal(it, it.symbol, insert.relation.computedAlias)
                }

                val updateCtx = Compilation(innerScope, sql)

                sql.prefix(" ", "\n,").forEach(assignments) {
                    val ref = it.reference

                    if (ref is Column<*>) {
                        sql.addIdentifier(ref.symbol)
                    } else {
                        sql.addError("can't update a non-column reference")
                    }

                    sql.addSql(" = ")
                    updateCtx.compileExpr(it.expr)
                }
            }

            return nonEmpty
        }

        fun compileUpdate(update: BuiltUpdate) = sql.compileUpdate(update,
            compileWiths = { type, withs -> compileWiths(type, withs) },
            compileRelation = { compileRelation(it) },
            compileAssignment = {
                compileSetLhs(it.reference)
                sql.addSql(" = ")
                compileExpr(it.expr)
            },
            compileExpr = { compileExpr(it, false) }
        )

        override fun excluded(reference: Reference<*>) {
            sql.addSql("EXCLUDED.")

            when (reference) {
                is Column<*> -> sql.addIdentifier(reference.symbol)
                else -> compileReference(reference)
            }
        }

        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) {
            compileReference(value)
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) {
            compileSubqueryExpr(subquery)
        }

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) {
            compileAggregatable(aggregatable)
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) {
            compileCastDataType(to)
        }

        override fun window(window: BuiltWindow) {
            compileWindow(window)
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

        fun compileDelete(delete: BuiltDelete) {
            val withs = delete.query.withs

            compileWiths(delete.query.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("\nDELETE FROM ")

            sql.compileQueryBody(
                delete.query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) }
            )
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
            is BuiltUpdate -> compilation.compileUpdate(dml)
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