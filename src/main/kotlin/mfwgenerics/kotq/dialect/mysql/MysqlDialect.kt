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
            ordinals.takeIf { it.isNotEmpty() }?.let { orderBy ->
                sql.addSql("ORDER BY ")

                var sep = ""

                orderBy.forEach {
                    sql.addSql(sep)

                    val orderKey = it.toOrderKey()

                    compileExpr(orderKey.expr, false)

                    sql.addSql(" ${orderKey.order.sql}")

                    sep = ", "
                }
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

            var prefix = ""

            window.partitions.from?.let {
                sql.addSql(names.nameOf(it))

                prefix = " "
            }

            if (!partitionedBy.isNullOrEmpty()) {
                sql.addSql("${prefix}PARTITION BY ")
                var sep = ""
                partitionedBy.forEach {
                    sql.addSql(sep)
                    compileExpr(it, false)
                    sep = ", "
                }

                prefix = " "
            }

            if (orderBy.isNotEmpty()) {
                sql.addSql(prefix)
                compileOrderBy(orderBy)

                prefix = " "
            }

            window.type?.let { windowType ->
                sql.addSql(prefix)
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
                            var sep = ""
                            if (enclosed) sql.addSql("(")
                            expr.args.forEach {
                                sql.addSql(sep)
                                compileExpr(it)
                                sep = " ${expr.type.sql} "
                            }
                            if (enclosed) sql.addSql(")")
                        }
                        OperationFixity.APPLY -> {
                            var sep = ""
                            sql.addSql(expr.type.sql)
                            sql.addSql("(")
                            expr.args.forEach {
                                sql.addSql(sep)
                                compileExpr(it, false)
                                sep = ", "
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

                    var sep = ""
                    sql.addSql(built.expr.type.sql)
                    sql.addSql("(")
                    built.expr.args.forEach {
                        sql.addSql(sep)
                        compileAggregatable(it)
                        sep = ", "
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
                is Subquery -> compileSelect(baseRelation.of.buildQuery())
            }

            sql.addSql(names[relation.computedAlias].ident)
        }

        fun compileQueryWhere(query: QueryWhere) {
            compileRelation(query.relation)

            sql.addSql("\n")

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

            body.groupBy.takeIf { it.isNotEmpty() }?.let { groupBy ->
                sql.addSql("\nGROUP BY ")

                var sep = ""

                groupBy.forEach {
                    sql.addSql(sep)
                    compileExpr(it, false)
                    sep = ", "
                }
            }

            body.having?.let {
                sql.addSql("\nHAVING ")
                compileExpr(it, false)
            }

            body.windows.takeIf { it.isNotEmpty() }?.let { windows ->
                var sep = "\nWINDOW "

                windows.forEach {
                    sql.addSql(sep)

                    sql.addSql(names.nameOf(it.label))
                    sql.addSql(" AS ")
                    sql.addSql("(")
                    compileWindow(it.window.buildWindow())
                    sql.addSql(")")
                    sep = ",\n"
                }
            }
        }

        fun compileSelect(select: BuiltSelectQuery) {
            val withs = select.body.where.withs

            if (withs.isNotEmpty()) {
                var sep = when (select.body.where.withType) {
                    WithType.RECURSIVE -> "WITH RECURSIVE "
                    WithType.NOT_RECURSIVE -> "WITH "
                }

                withs.forEach {
                    sql.addSql(sep)

                    val subquery = names[it.alias]

                    sql.addSql(subquery.ident)
                    sql.addSql(" AS (")

                    Compilation(
                        names = subquery.scope,
                        sql = sql
                    ).compileSelect(it.query)

                    sql.addSql(")")

                    sep = ", "
                }

                sql.addSql("\n")
            }

            sql.addSql("SELECT")

            var sep = ""

            select.selected
                .asSequence()
                .flatMap { it.namedExprs() }
                .forEach {
                    sql.addSql("$sep\n")

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
                    sep = ","
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