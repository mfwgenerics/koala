package mfwgenerics.kotq.dialect.mysql

import mfwgenerics.kotq.*
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.*
import mfwgenerics.kotq.window.*

class MysqlDialect: SqlDialect {
    private class Compilation(
        val names: Scope,
        val sql: SqlTextBuilder = SqlTextBuilder()
    ) {
        fun compileReference(name: AliasedName<*>) {
            sql.addSql(names[name])
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
                    sql.addSql(names.nameOf(it))
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

        fun compileExpr(expr: Expr<*>, enclosed: Boolean = true) {
            when (expr) {
                is OperationExpr -> {
                    when (expr.type.fixity) {
                        OperationFixity.PREFIX -> {
                            if (enclosed) sql.addSql("(")
                            sql.addSql(expr.type.sql)
                            sql.addSql(" ")
                            compileExpr(expr.args.single(), false)
                            if (enclosed) sql.addSql(")")
                        }
                        OperationFixity.POSTFIX -> {
                            if (enclosed) sql.addSql("(")
                            compileExpr(expr.args.single(), false)
                            sql.addSql(" ")
                            sql.addSql(expr.type.sql)
                            if (enclosed) sql.addSql(")")
                        }
                        OperationFixity.INFIX -> {
                            if (enclosed) sql.addSql("(")

                            sql.prefix("", " ${expr.type.sql} ").forEach(expr.args) {
                                compileExpr(it)
                            }

                            if (enclosed) sql.addSql(")")
                        }
                        OperationFixity.APPLY -> {
                            sql.addSql(expr.type.sql)
                            sql.addSql("(")
                            sql.prefix("", ", ").forEach(expr.args) {
                                compileExpr(it, false)
                            }
                            sql.addSql(")")
                        }
                    }
                }
                is Literal -> sql.addValue(expr.value)
                is Reference -> {
                    compileReference(expr.buildAliased())
                }
                is AggregatedExpr -> {
                    val built = expr.buildAggregated()

                    sql.addSql(built.expr.type.sql)
                    sql.addSql("(")
                    sql.prefix("", ", ").forEach(built.expr.args) {
                        compileAggregatable(it)
                    }
                    sql.addSql(")")

                    // mysql doesn't actually support this - maybe have convert to a CASE
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
            }
        }

        fun compileRelation(relation: QueryRelation) {
            when (val baseRelation = relation.relation) {
                is Table -> {
                    sql.addSql(baseRelation.name)
                    sql.addSql(" ")
                }
                is Subquery -> compileSelect(baseRelation.of.buildSelect())
            }

            sql.addSql(names[relation.computedAlias].ident)
        }

        fun compileQueryWhere(query: QueryWhere) {
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

        fun compileSelectBody(body: SelectBody) {
            compileQueryWhere(body.where)

            sql.prefix("\nGROUP BY", ", ").forEach(body.groupBy) {
                compileExpr(it, false)
            }

            body.having?.let {
                sql.addSql("\nHAVING ")
                compileExpr(it, false)
            }

            sql.prefix("\nWINDOW ", "\n, ").forEach(body.windows) {
                sql.addSql(names.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(it.window.buildWindow())
                sql.addSql(")")
            }
        }

        fun compileSelect(select: BuiltSelectQuery) {
            val withs = select.body.where.withs

            sql
                .prefix(
                    when (select.body.where.withType) {
                        WithType.RECURSIVE -> "WITH RECURSIVE "
                        WithType.NOT_RECURSIVE -> "WITH "
                    },
                    ", "
                )
                .forEach(withs) {
                    val subquery = names[it.alias]

                    sql.addSql(subquery.ident)
                    sql.addSql(" AS (")

                    Compilation(
                        names = subquery.scope,
                        sql = sql
                    ).compileSelect(it.query)

                    sql.addSql(")")
                }

            if (withs.isNotEmpty()) sql.addSql("\n")

            val selectPrefix = sql.prefix("SELECT ", "\n, ")

            select.selected
                .asSequence()
                .flatMap { it.namedExprs() }
                .forEach {
                    selectPrefix.next {
                        when (it) {
                            is LabeledExpr -> {
                                compileExpr(it.expr, false)
                                sql.addSql(" ")
                                sql.addSql(names.nameOf(it.name))
                            }
                            is LabeledName -> {
                                val name = it.name

                                compileReference(name)

                                if (name.aliases.isNotEmpty()) {
                                    sql.addSql(" ")
                                    sql.addSql(names.nameOf(it.name))
                                }
                            }
                        }
                    }
                }

            sql.addSql("\nFROM ")

            compileSelectBody(select.body)

            sql.addSql("\n")
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
    }

    override fun compile(statement: Statement): SqlText {
        if (statement is BuiltSelectQuery) {
            val registry = NameRegistry()

            val scope = Scope(registry)

            statement.populateScope(scope)

            val compilation = Compilation(
                names = scope
            )

            compilation.compileSelect(statement)

            return compilation.sql.toSql()
        }

        error("can't compile $statement")
    }
}