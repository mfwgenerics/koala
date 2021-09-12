package mfwgenerics.kotq.h2

import mfwgenerics.kotq.data.*
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.built.ColumnDefaultExpr
import mfwgenerics.kotq.ddl.built.ColumnDefaultValue
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.*
import mfwgenerics.kotq.dsl.value
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.sql.*
import mfwgenerics.kotq.window.*
import mfwgenerics.kotq.window.built.BuiltWindow
import kotlin.reflect.KClass

class H2Dialect: SqlDialect {
    override fun ddl(diff: SchemaDiff): List<SqlText> {
        val results = mutableListOf<SqlText>()

        diff.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            Compilation(
                Scope(NameRegistry()),
                sql
            ).compileCreateTable(sql, table)

            results.add(sql.toSql())
        }

        return results
    }

    private class Compilation(
        val scope: Scope,
        override val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)
    ): ExpressionCompiler {
        private fun compileDefaultExpr(expr: Expr<*>) {
            compileExpr(expr)
        }

        private fun compileDataType(type: UnmappedDataType<*>) {
            computeWithColumnDefaults(type) {
                when (it) {
                    DATE -> TODO()
                    DATETIME -> TODO()
                    is DECIMAL -> TODO()
                    DOUBLE -> sql.addSql("DOUBLE")
                    FLOAT -> sql.addSql("REAL")
                    INSTANT -> sql.addSql("TIMESTAMP WITH TIME ZONE")
                    SMALLINT -> sql.addSql("SMALLINT")
                    INTEGER -> sql.addSql("INTEGER")
                    TINYINT -> sql.addSql("TINYINT")
                    is RAW -> TODO()
                    TIME -> TODO()
                    is VARBINARY -> TODO()
                    is VARCHAR -> {
                        sql.addSql("VARCHAR")
                        sql.parenthesize {
                            sql.addSql("${it.maxLength}")
                        }
                    }
                    BIGINT -> TODO()
                    BOOLEAN -> sql.addSql("BOOL")
                    TEXT -> sql.addSql("TEXT")
                    else -> null
                }
            }
        }

        fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
            sql.addSql("CREATE TABLE IF NOT EXISTS ")

            sql.addIdentifier(table.relvarName)
            sql.parenthesize {
                sql.prefix("", ", ").forEach(table.columns) {
                    sql.addSql("\n")
                    val def = it.builtDef

                    sql.addIdentifier(it.symbol)
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
                sql.addSql("\n")
            }
        }

        fun compileReference(name: Reference<*>) {
            sql.addResolved(scope.resolve(name))
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
            when (type) {
                DATE -> TODO()
                DATETIME -> TODO()
                is DECIMAL -> TODO()
                DOUBLE -> TODO()
                FLOAT -> TODO()
                INSTANT -> TODO()
                SMALLINT -> TODO()
                INTEGER -> sql.addSql("INTEGER")
                TINYINT -> TODO()
                is RAW -> TODO()
                TIME -> TODO()
                TINYINT.UNSIGNED -> TODO()
                is VARBINARY -> TODO()
                is VARCHAR -> TODO()
                BIGINT -> TODO()
                SMALLINT.UNSIGNED -> TODO()
                INTEGER.UNSIGNED -> TODO()
                BIGINT.UNSIGNED -> TODO()
            }
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) =
            compileCastDataType(to)

        fun compileQuery(outerSelect: List<SelectedExpr<*>>, query: BuiltSubquery) {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            when (query) {
                is BuiltSelectQuery -> compilation.compileSelect(outerSelect, query)
                is BuiltValuesQuery -> compilation.compileValues(query)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(emptyList(), subquery)
            }
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) =
            compileSubqueryExpr(subquery)

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            sql.compileExpr(expr, emitParens, this)
        }

        fun compileRelabels(labels: LabelList) {
            sql.parenthesize {
                sql.prefix("", ", ").forEach(labels.values) {
                    sql.addIdentifier(scope.nameOf(it))
                }
            }
        }

        fun compileRelation(relation: BuiltRelation) {
            val explicitLabels = when (val baseRelation = relation.relation) {
                is Relvar -> {
                    sql.addIdentifier(baseRelation.relvarName)
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
                    null
                }
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
            ).compileSelect(outerSelect, selectQuery)
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

        fun compileWindows(windows: List<LabeledWindow>) {
            sql.prefix("\nWINDOW ", "\n, ").forEach(windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(it.window.buildWindow())
                sql.addSql(")")
            }
        }

        fun compileSelect(outerSelect: List<SelectedExpr<*>>, select: BuiltSelectQuery) {
            val withs = select.body.withs

            compileWiths(select.body.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            val selectPrefix = sql.prefix("SELECT ", "\n, ")

            (select.selected.takeIf { it.isNotEmpty() }?:outerSelect)
                .forEach {
                    selectPrefix.next {
                        compileExpr(it.expr, false)
                        sql.addSql(" ")
                        sql.addIdentifier(scope.nameOf(it.name))
                    }
                }

            if (select.standalone) return
            sql.addSql("\nFROM ")

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
                check (select.body.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(select.body.offset))
            }

            select.body.locking?.let { locking ->
                when (locking) {
                    LockMode.SHARE -> sql.addSql("\nFOR UPDATE")
                    LockMode.UPDATE -> sql.addSql("\nFOR SHARED")
                }
            }
        }

        fun compileValues(query: BuiltValuesQuery) {
            val values = query.values

            val columns = values.columns
            val iter = values.rowIterator()

            val rowPrefix = sql.prefix("VALUES ", "\n, ")

            while (iter.next()) {
                rowPrefix.next {
                    sql.addSql("(")
                    sql.prefix("", ", ").forEach(columns.values) {
                        @Suppress("unchecked_cast")
                        sql.addLiteral(Literal(
                            it.type as KClass<Any>,
                            iter.row.getOrNull(it)
                        ))
                    }
                    sql.addSql(")")
                }
            }
        }

        fun compileInsert(insert: BuiltInsert) {
            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("INSERT INTO ")

            val relvar = when (val relation = insert.relation.relation) {
                is Relvar -> relation
                else -> error("unsuitable subject for insert")
            }

            val tableColumnMap = relvar.columns.associateBy { it }
            val columns = insert.query.columns

            sql.addIdentifier(relvar.relvarName)
            sql.addSql(" ")

            sql.parenthesize {
                sql.prefix("", ", ").forEach(columns.values) {
                    val column = checkNotNull(tableColumnMap[it]) {
                        "can't insert $it into ${relvar.relvarName}"
                    }

                    sql.addIdentifier(column.symbol)
                }
            }

            sql.addSql("\n")

            compileQuery(emptyList(), insert.query)
        }

        fun compileUpdate(update: BuiltUpdate) {
            val query = update.query

            compileWiths(query.withType, query.withs)

            if (query.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("UPDATE ")

            compileRelation(update.query.relation)

            sql.addSql("\nSET ")

            val updatePrefix = sql.prefix("", ", ")

            check (query.joins.isEmpty()) {
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

            check (select.query.setOperations.isEmpty())

            if (select.query.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.query.orderBy)

            select.query.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(value(it))
            }

            if (select.query.offset != 0) {
                check (select.query.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(select.query.offset))
            }

            check(select.query.locking == null)
        }
    }

    override fun compile(statement: BuiltStatement): SqlText {
        val registry = NameRegistry()
        val scope = Scope(registry)

        val compilation = Compilation(
            scope = scope
        )

        statement.populateScope(scope)

        when (statement) {
            is BuiltSelectQuery -> compilation.compileSelect(emptyList(), statement)
            is BuiltValuesQuery -> compilation.compileValues(statement)
            is BuiltInsert -> compilation.compileInsert(statement)
            is BuiltUpdate -> compilation.compileUpdate(statement)
            is BuiltDelete -> compilation.compileDelete(statement)
        }

        return compilation.sql.toSql()
    }
}