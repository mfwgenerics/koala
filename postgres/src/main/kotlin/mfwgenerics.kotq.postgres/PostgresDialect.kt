package mfwgenerics.kotq.postgres

import mfwgenerics.kotq.data.*
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.ColumnDefaultExpr
import mfwgenerics.kotq.ddl.built.ColumnDefaultValue
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.dsl.literal
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.sql.*
import mfwgenerics.kotq.window.*
import mfwgenerics.kotq.window.built.BuiltWindow
import kotlin.reflect.KClass

class PostgresDialect: SqlDialect {
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

    private fun compileSerialType(sql: SqlTextBuilder, type: DataType<*>) {
        when (type) {
            SMALLINT -> sql.addSql("SMALLSERIAL")
            INTEGER -> sql.addSql("SERIAL")
            BIGINT -> sql.addSql("BIGSERIAL")
            else -> error("no serial type corresponds to $type")
        }
    }

    private fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
        sql.addSql("CREATE TABLE ")

        sql.addIdentifier(table.relvarName)
        sql.parenthesize {
            val comma = sql.prefix("\n", ",\n")

            comma.forEach(table.columns) {
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

            sql.addSql("\n")
        }
    }

    override fun ddl(diff: SchemaDiff): List<SqlText> {
        val results = mutableListOf<SqlText>()

        diff.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            compileCreateTable(sql, table)

            results.add(sql.toSql().also { println(it) })
        }

        return results
    }

    private class Compilation(
        val scope: Scope,
        val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)
    ) {
        fun compileReference(name: Reference<*>) {
            sql.addResolved(scope.resolve(name))
        }

        fun compileOrderBy(ordinals: List<Ordinal<*>>) {
            sql.prefix("ORDER BY ", ", ").forEach(ordinals) {
                val orderKey = it.toOrderKey()

                compileExpr(orderKey.expr, false)

                sql.addSql(" ${orderKey.order.sql}")
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

        fun compileCastDataType(type: DataType<*>) {
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

        fun exhaustive(value: Any?) { }

        fun compileSetLhs(expr: Reference<*>, emitParens: Boolean = true) {
            sql.addIdentifier(scope.resolve(expr).innerName)
        }

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            exhaustive(when (expr) {
                is OperationExpr<*> -> {
                    when (expr.type.fixity) {
                        OperationFixity.PREFIX -> {
                            sql.parenthesize(emitParens) {
                                sql.addSql(expr.type.sql)
                                sql.addSql(" ")
                                compileExpr(expr.args.single(), false)
                            }
                        }
                        OperationFixity.POSTFIX -> {
                            sql.parenthesize(emitParens) {
                                compileExpr(expr.args.single(), false)
                                sql.addSql(" ")
                                sql.addSql(expr.type.sql)
                            }
                        }
                        OperationFixity.INFIX -> {
                            sql.parenthesize(emitParens) {
                                sql.prefix("", " ${expr.type.sql} ").forEach(expr.args) {
                                    compileExpr(it)
                                }
                            }
                        }
                        OperationFixity.APPLY -> {
                            sql.addSql(expr.type.sql)
                            sql.parenthesize {
                                sql.prefix("", ", ").forEach(expr.args) {
                                    compileExpr(it, false)
                                }
                            }
                        }
                    }
                }
                is Literal<*> -> sql.addLiteral(expr)
                is Reference<*> -> { compileReference(expr) }
                is AggregatedExpr<*> -> {
                    val built = expr.buildAggregated()

                    sql.addSql(built.expr.type.sql)
                    sql.parenthesize {
                        sql.prefix("", ", ").forEach(built.expr.args) {
                            compileAggregatable(it)
                        }
                    }

                    // h2 doesn't actually support this - maybe have convert to a CASE
                    built.filter?.let { filter ->
                        sql.addSql(" FILTER(WHERE ")
                        compileExpr(filter, false)
                        sql.addSql(")")
                    }

                    built.over?.let { window ->
                        sql.addSql(" OVER (")
                        compileWindow(window)
                        sql.addSql(")")
                    }
                }
                is CastExpr<*> -> {
                    sql.addSql("CAST")
                    sql.parenthesize {
                        compileExpr(expr.of, false)
                        sql.addSql(" AS ")
                        compileCastDataType(expr.type)
                    }
                }
                is SubqueryExpr -> compileSubqueryExpr(expr.subquery)
                is ExprListExpr<*> -> sql.parenthesize {
                    sql.prefix("", ", ").forEach(expr.exprs) {
                        compileExpr(it, false)
                    }
                }
                is ComparedQuery<*> -> {
                    sql.addSql(expr.type)
                    compileSubqueryExpr(expr.subquery)
                }
            })
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

            query.joins.asReversed().forEach { join ->
                sql.addSql("\n")
                sql.addSql(join.type.sql)
                sql.addSql(" ")
                compileRelation(join.to)
                sql.addSql(" ON ")
                compileExpr(join.on, false)
            }

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

            (select.selected.takeIf { it.isNotEmpty() }?:outerSelect)
                .forEach {
                    selectPrefix.next {
                        compileExpr(it.expr, false)
                        sql.addSql(" ")
                        sql.addIdentifier(scope.nameOf(it.name))
                    }
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
                "JOIN in update not supported"
            }

            update.assignments
                .forEach {
                    updatePrefix.next {
                        compileSetLhs(it.reference)
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