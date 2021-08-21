package mfwgenerics.kotq.dialect.h2

import mfwgenerics.kotq.ddl.DataType
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.dsl.Relvar
import mfwgenerics.kotq.dsl.Subquery
import mfwgenerics.kotq.query.WithType
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.Distinctness
import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.sql.NameRegistry
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.sql.SqlTextBuilder
import mfwgenerics.kotq.window.*
import mfwgenerics.kotq.window.built.BuiltWindow

class H2Dialect: SqlDialect {
    private class Compilation(
        val scope: Scope,
        val sql: SqlTextBuilder = SqlTextBuilder()
    ) {
        fun compileReference(name: Reference<*>) {
            sql.addSql(scope[name])
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
                DataType.DATE -> TODO()
                DataType.DATETIME -> TODO()
                is DataType.DECIMAL -> TODO()
                DataType.DOUBLE -> TODO()
                DataType.FLOAT -> TODO()
                DataType.INSTANT -> TODO()
                DataType.INT16 -> TODO()
                DataType.INT32 -> sql.addSql("INTEGER")
                DataType.INT64 -> TODO()
                DataType.INT8 -> TODO()
                is DataType.RAW -> TODO()
                DataType.TIME -> TODO()
                DataType.UINT16 -> TODO()
                DataType.UINT32 -> TODO()
                DataType.UINT64 -> TODO()
                DataType.UINT8 -> TODO()
                is DataType.VARBINARY -> TODO()
                is DataType.VARCHAR -> TODO()
            }
        }

        fun compileExpr(expr: Expr<*>, emitParens: Boolean = true) {
            when (expr) {
                is OperationExpr -> {
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
                is Literal -> sql.addValue(expr.value)
                is Reference -> { compileReference(expr) }
                is AggregatedExpr -> {
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
                is CastExpr -> {
                    sql.addSql("CAST")
                    sql.parenthesize {
                        compileExpr(expr.of, false)
                        sql.addSql(" AS ")
                        compileCastDataType(expr.type)
                    }
                }
            }
        }

        fun compileRelation(relation: BuiltRelation) {
            val relationScope = scope[relation.computedAlias]

            val explicitLabels = when (val baseRelation = relation.relation) {
                is Relvar -> {
                    sql.addSql(baseRelation.relvarName)
                    null
                }
                is Subquery -> {
                    sql.parenthesize {
                        Compilation(
                            relationScope.scope,
                            sql = sql
                        ).compileQuery(emptyList(), baseRelation.of)
                    }

                    if (baseRelation.of is BuiltValuesQuery) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
                }
            }

            sql.addSql(" ")
            sql.addSql(relationScope.ident)

            explicitLabels?.let { labels ->
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(labels.values) {
                        sql.addSql(scope.nameOf(it))
                    }
                }
            }
        }

        fun compileQueryWhere(query: BuiltWhere) {
            compileRelation(query.relation)

            query.joins.forEach { join ->
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

        fun compileSelectBody(body: BuiltSelectBody) {
            compileQueryWhere(body.where)

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
            outerSelect: List<Labeled<*>>,
            operation: BuiltSetOperation
        ) {
            sql.addSql("\n")
            sql.addSql(operation.type.sql)
            sql.addSql("\n")

            Compilation(
                scope.innerScope().also {
                    operation.body.populateScope(it)
                },
                sql
            ).compileSelect(outerSelect, operation.body)
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
                    val subquery = scope[it.alias]

                    sql.addSql(subquery.ident)
                    sql.addSql(" AS (")

                    Compilation(
                        scope = subquery.scope,
                        sql = sql
                    ).compileQuery(emptyList(), it.query)

                    sql.addSql(")")
                }
        }

        fun compileSelect(outerSelect: List<Labeled<*>>, select: BuiltSelectQuery) {
            val withs = select.body.where.withs

            compileWiths(select.body.where.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            val selectPrefix = sql.prefix("SELECT ", "\n, ")

            (select.selected.takeIf { it.isNotEmpty() }?:outerSelect)
                .forEach {
                    selectPrefix.next {
                        compileExpr(it.expr, false)
                        sql.addSql(" ")
                        sql.addSql(scope.nameOf(it.name))
                    }
                }

            sql.addSql("\nFROM ")

            compileSelectBody(select.body)

            select.setOperations.forEach {
                compileSetOperation(select.selected, it)
            }

            if (select.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.orderBy)

            select.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addValue(it)
            }

            if (select.offset != 0) {
                check (select.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addValue(select.offset)
            }

            select.locking?.let { locking ->
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
                        sql.addValue(iter[it])
                    }
                    sql.addSql(")")
                }
            }
        }

        fun compileQuery(outerSelect: List<Labeled<*>>, query: BuiltQuery) {
            when (query) {
                is BuiltSelectQuery -> compileSelect(outerSelect, query)
                is BuiltValuesQuery -> compileValues(
                    query
                )
            }
        }

        fun compileInsert(insert: BuiltInsert) {
            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("INSERT INTO ")

            val relvar = when (val relation = insert.relation.relation) {
                is Relvar -> relation
                is Subquery -> error("can not insert into subquery")
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

            compileQuery(emptyList(), insert.query)
        }
    }

    override fun compile(statement: BuiltStatement): SqlText {
        if (statement is BuiltSelectQuery) {
            val registry = NameRegistry()

            val scope = Scope(registry)

            statement.populateScope(scope)

            val compilation = Compilation(
                scope = scope
            )

            compilation.compileSelect(emptyList(), statement)

            return compilation.sql.toSql()
        } else if (statement is BuiltValuesQuery) {
            val registry = NameRegistry()

            val scope = Scope(registry)

            statement.populateScope(scope)

            val compilation = Compilation(
                scope = scope
            )

            compilation.compileValues(statement)

            return compilation.sql.toSql()
        } else if (statement is BuiltInsert) {
            val registry = NameRegistry()

            val scope = Scope(registry)

            statement.populateScope(scope)

            val compilation = Compilation(
                scope = scope
            )

            compilation.compileInsert(statement)

            return compilation.sql.toSql()
        }

        error("can't compile $statement")
    }
}