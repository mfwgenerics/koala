package mfwgenerics.kotq.dialect.mysql

import mfwgenerics.kotq.*
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.NameRegistry
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.sql.SqlTextBuilder
import mfwgenerics.kotq.window.*

class MysqlDialect: SqlDialect {
    private class Compilation(
        val registry: NameRegistry = NameRegistry(),
        val names: Scope = Scope(registry),
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

            if (!partitionedBy.isNullOrEmpty()) {
                sql.addSql("PARTITION BY ")
                var sep = ""
                partitionedBy.forEach {
                    sql.addSql(sep)
                    compileExpr(it, false)
                    sep = ", "
                }
            }

            if (orderBy.isNotEmpty()) {
                sql.addSql(" ")
                compileOrderBy(orderBy)
            }

            window.type?.let { windowType ->
                sql.addSql(" ")
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

            window.type
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

            sql.addSql(names[relation.computedAlias])
        }

        fun compileQueryWhere(query: QueryWhere) {
            compileRelation(query.relation)

            sql.addSql("\n")

            query.joins.forEach { join ->
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
        }

        fun compileSelect(select: SelectQuery) {
            select.populateScope(names)

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
                            sql.addSql(registry[it.name])
                        }
                        is LabeledName -> {
                            val name = it.name

                            compileReference(name)

                            if (name.aliases.isNotEmpty()) {
                                sql.addSql(" ")
                                sql.addSql(registry[it.name])
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
        val compilation = Compilation()

        if (statement is SelectQuery) {
            compilation.compileSelect(statement)
        }

        return compilation.sql.toSql()
    }
}