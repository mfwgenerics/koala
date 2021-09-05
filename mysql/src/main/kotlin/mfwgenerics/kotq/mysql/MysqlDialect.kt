package mfwgenerics.kotq.mysql

import mfwgenerics.kotq.data.*
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.ColumnDefaultExpr
import mfwgenerics.kotq.ddl.built.ColumnDefaultValue
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.*
import mfwgenerics.kotq.dsl.literal
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.sql.*
import mfwgenerics.kotq.window.FrameRangeMarker
import mfwgenerics.kotq.window.built.BuiltWindow
import kotlin.reflect.KClass

class MysqlDialect: SqlDialect {
    private fun compileDefaultExpr(sql: SqlTextBuilder, expr: Expr<*>) {
        when (expr) {
            is Literal -> sql.addLiteral(expr)
            else -> error("not implemented")
        }
    }

    private fun compileDataType(sql: SqlTextBuilder, type: DataType<*>) {
        when (type) {
            DATE -> TODO()
            DATETIME -> TODO()
            is DECIMAL -> TODO()
            DOUBLE -> TODO()
            FLOAT -> TODO()
            INSTANT -> TODO()
            SMALLINT -> sql.addSql("SMALLINT")
            INTEGER -> sql.addSql("INTEGER")
            TINYINT -> TODO()
            is RAW -> TODO()
            TIME -> TODO()
            is VARBINARY -> TODO()
            is VARCHAR -> {
                sql.addSql("VARCHAR")
                sql.parenthesize {
                    sql.addSql("${type.maxLength}")
                }
            }
            BIGINT -> TODO()
            TINYINT.UNSIGNED -> TODO()
            SMALLINT.UNSIGNED -> TODO()
            INTEGER.UNSIGNED -> TODO()
            BIGINT.UNSIGNED -> TODO()
        }
    }

    private fun compileColumnDef(sql: SqlTextBuilder, column: TableColumn<*>) {
        val def = column.builtDef

        sql.addSql(column.symbol)
        sql.addSql(" ")
        compileDataType(sql, def.columnType.dataType)

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
            compileDefaultExpr(sql, finalExpr)
        }
    }

    private fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
        sql.addSql("CREATE TABLE ")

        sql.addSql(table.relvarName)
        sql.parenthesize {
            val comma = sql.prefix("\n", ",\n")

            comma.forEach(table.columns) {
                compileColumnDef(sql, it)
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

            sql.addSql("\n")
        }
    }

    override fun ddl(diff: SchemaDiff): List<SqlText> {
        val results = mutableListOf<(SqlTextBuilder) -> Unit>()

        diff.tables.created.forEach { (_, table) ->
            results.add { sql ->
                compileCreateTable(sql, table)
            }
        }

        diff.tables.altered.forEach { (_, table) ->
            table.alteration.columns.created.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.value.relvarName)
                    sql.addSql(" ADD COLUMN ")
                    compileColumnDef(sql, column)
                }
            }

            table.alteration.columns.altered.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.value.relvarName)
                    sql.addSql(" MODIFY COLUMN ")
                    compileColumnDef(sql, column.value)
                }
            }

            table.alteration.columns.dropped.forEach { column ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.value.relvarName)
                    sql.addSql(" DROP COLUMN ")
                    sql.addIdentifier(column)
                }
            }
        }

        return results.map {
            SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS).also(it).toSql()
        }
    }

    private class Compilation(
        val scope: Scope,
        override val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS)
    ): ExpressionCompiler {
        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) {
            compileReference(value)
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) {
            compileSubqueryExpr(subquery)
        }

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) {
            compileAggregatable(aggregatable)
        }

        override fun <T : Any> dataTypeForCast(to: DataType<T>) {
            compileCastDataType(to)
        }

        override fun window(window: BuiltWindow) {
            compileWindow(window)
        }

        fun compileReference(name: Reference<*>) {
            sql.addResolved(scope.resolve(name))
        }

        fun compileOrderBy(ordinals: List<Ordinal<*>>) = sql.orderByClause(ordinals) {
            compileExpr(it, false)
        }

        fun compileAggregatable(aggregatable: BuiltAggregatable) {
            if (aggregatable.distinct == Distinctness.DISTINCT) sql.addSql("DISTINCT ")

            compileExpr(aggregatable.expr, false)

            if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
        }

        fun compileRangeMarker(direction: String, marker: FrameRangeMarker<*>) =
            sql.compileRangeMarker(direction, marker) { compileExpr(it) }

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

        fun compileCastDataType(type: DataType<*>) {
            when (type) {
                DATE -> TODO()
                DATETIME -> TODO()
                is DECIMAL -> TODO()
                DOUBLE -> TODO()
                FLOAT -> TODO()
                INSTANT -> TODO()
                SMALLINT -> TODO()
                INTEGER -> sql.addSql("SIGNED")
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

        fun compileQuery(outerSelect: List<SelectedExpr<*>>, query: BuiltSubquery, forInsert: Boolean) {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            when (query) {
                is BuiltSelectQuery -> compilation.compileSelect(outerSelect, query)
                is BuiltValuesQuery -> compilation.compileValues(query, forInsert)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(emptyList(), subquery, false)
            }
        }

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            return expr.compile(
                emitParens,
                this
            )
        }

        fun compileRelation(relation: BuiltRelation) {
            val explicitLabels = when (val baseRelation = relation.relation) {
                is Relvar -> {
                    sql.addSql(baseRelation.relvarName)
                    null
                }
                is Subquery -> {
                    val innerScope = scope.innerScope()

                    baseRelation.of.populateScope(innerScope)

                    sql.parenthesize {
                        Compilation(
                            innerScope,
                            sql = sql
                        ).compileQuery(emptyList(), baseRelation.of, false)
                    }

                    if (baseRelation.of is BuiltValuesQuery) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
                }
                is Cte -> {
                    sql.addSql(scope[baseRelation])
                    null
                }
            }

            sql.addSql(" ")
            sql.addSql(scope[relation.computedAlias])

            explicitLabels?.let { labels ->
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(labels.values) {
                        sql.addSql(scope.nameOf(it))
                    }
                }
            }
        }

        fun compileQueryWhere(query: BuiltQueryBody) {
            compileRelation(query.relation)

            sql.compileJoins(query.joins.asReversed(),
                compileRelation = { compileRelation(it) },
                compileExpr = { compileExpr(it, false) }
            )

            query.where?.let {
                sql.addSql("\nWHERE ")
                compileExpr(it, false)
            }
        }

        fun compileSelectBody(body: BuiltQueryBody) {
            compileQueryWhere(body)

            sql.prefix("\nGROUP BY ", ", ").forEach(body.groupBy) {
                compileExpr(it, false)
            }

            body.having?.let {
                sql.addSql("\nHAVING ")
                compileExpr(it, false)
            }

            sql.prefix("\nWINDOW ", "\n, ").forEach(body.windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(it.window.buildWindow())
                sql.addSql(")")
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
                    sql.addSql(" AS (")

                    Compilation(
                        scope = innerScope,
                        sql = sql
                    ).compileQuery(emptyList(), it.query, false)

                    sql.addSql(")")
                }
        }

        fun compileSelect(outerSelect: List<SelectedExpr<*>>, select: BuiltSelectQuery) {
            val withs = select.body.withs

            compileWiths(select.body.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            val selectPrefix = sql.prefix("SELECT ", "\n, ")

            sql.selectClause(select.selected.takeIf { it.isNotEmpty() }?:outerSelect) {
                compileExpr(it.expr, false)
                sql.addSql(" ")
                sql.addSql(scope.nameOf(it.name))
            }

            sql.addSql("\nFROM ")

            compileSelectBody(select.body)

            select.body.setOperations.forEach {
                compileSetOperation(select.selected, it)
            }

            if (select.body.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.body.orderBy)

            select.body.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(literal(it))
            }

            if (select.body.offset != 0) {
                check (select.body.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(literal(select.body.offset))
            }

            select.body.locking?.let { locking ->
                when (locking) {
                    LockMode.SHARE -> sql.addSql("\nFOR UPDATE")
                    LockMode.UPDATE -> sql.addSql("\nFOR SHARED")
                }
            }
        }

        fun compileValues(query: BuiltValuesQuery, forInsert: Boolean) {
            val values = query.values

            val columns = values.columns
            val iter = values.rowIterator()

            val rowPrefix = sql.prefix("VALUES ", "\n, ")

            while (iter.next()) {
                rowPrefix.next {
                    if (!forInsert) sql.addSql("ROW ")

                    sql.addSql("(")
                    sql.prefix("", ", ").forEach(columns.values) {
                        @Suppress("unchecked_cast")
                        sql.addLiteral(Literal(
                            it.type as KClass<Any>,
                            iter[it]
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
                is Subquery -> error("can not insert into subquery")
                is Cte -> error("can not insert into CTE")
            }

            val tableColumnMap = relvar.columns.associateBy { it }
            val columns = insert.query.columns

            sql.addSql(relvar.relvarName)
            sql.addSql(" ")

            sql.parenthesize {
                sql.prefix("", ", ").forEach(columns.values) {
                    val column = checkNotNull(tableColumnMap[it]) {
                        "can't insert $it into ${relvar.relvarName}"
                    }

                    sql.addSql(column.symbol)
                }
            }

            sql.addSql("\n")

            compileQuery(emptyList(), insert.query, true)
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
            is BuiltValuesQuery -> compilation.compileValues(statement, false)
            is BuiltInsert -> compilation.compileInsert(statement)
            is BuiltUpdate -> compilation.compileUpdate(statement)
        }

        return compilation.sql.toSql()
    }
}