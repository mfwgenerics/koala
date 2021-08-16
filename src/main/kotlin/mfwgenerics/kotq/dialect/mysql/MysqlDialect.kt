package mfwgenerics.kotq.dialect.mysql

import mfwgenerics.kotq.*
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.sql.SqlTextBuilder

class MysqlDialect: SqlDialect {
    private class Compilation(
        val names: Scope = Scope(),
        val sql: SqlTextBuilder = SqlTextBuilder()
    ) {
        fun compileExpr(expr: Expr<*>) {
            when (expr) {
                is OperationExpr -> {
                    when (expr.type.fixity) {
                        OperationFixity.PREFIX -> {
                            sql.addSql("(")
                            sql.addSql(expr.type.sql)
                            sql.addSql(" ")
                            compileExpr(expr.args.single())
                            sql.addSql(")")
                        }
                        OperationFixity.POSTFIX -> {
                            sql.addSql("(")
                            compileExpr(expr.args.single())
                            sql.addSql(" ")
                            sql.addSql(expr.type.sql)
                            sql.addSql(")")
                        }
                        OperationFixity.INFIX -> {
                            var sep = ""
                            sql.addSql("(")
                            expr.args.forEach {
                                sql.addSql(sep)
                                compileExpr(it)
                                sep = " ${expr.type.sql} "
                            }
                            sql.addSql(")")
                        }
                        OperationFixity.APPLY -> {
                            var sep = ""
                            sql.addSql(expr.type.sql)
                            sql.addSql("(")
                            expr.args.forEach {
                                sql.addSql(sep)
                                compileExpr(it)
                                sep = ", "
                            }
                            sql.addSql(")")
                        }
                    }
                }
                is Constant -> sql.addValue(expr.value)
                is Reference -> {
                    val aliased = expr.toAliasedName()

                    sql.addSql(names[aliased])
                }
            }
        }

        fun compileQueryWhere(query: QueryWhere) {
            val relation = query.relation!!.relation

            when (relation) {
                is Table -> sql.addSql(relation.name)
                is Subquery -> compileSelect(relation.of.buildQuery())
            }

            sql.addSql("\n")

            query.joins.forEach { join ->
                sql.addSql(join.type.sql)
                sql.addSql(" ON ")
                compileExpr(join.on)
            }

            query.where?.let {
                sql.addSql("\nWHERE ")
                compileExpr(it)
            }
        }

        fun compileSelectBody(body: SelectBody) {
            compileQueryWhere(body.where)

            body.groupBy.takeIf { it.isNotEmpty() }?.let { groupBy ->
                sql.addSql("\nGROUP BY ")

                var sep = ""

                groupBy.forEach {
                    sql.addSql(sep)
                    compileExpr(it)
                    sep = ", "
                }
            }

            body.having?.let {
                sql.addSql("\nHAVING ")
                compileExpr(it)
            }
        }

        fun compileSelect(select: SelectQuery) {
            select.intoNameSet(names)

            sql.addSql("SELECT")

            var sep = ""

            select.selected
                .asSequence()
                .flatMap { it.namedExprs() }
                .forEach {
                    sql.addSql("$sep\n")
                    compileExpr(it.expr)
                    sql.addSql(" ")
                    sql.addSql(names[it.name])
                    sep = ", "
                }

            sql.addSql("\nFROM ")

            compileSelectBody(select.body)

            select.orderBy.takeIf { it.isNotEmpty() }?.let { orderBy ->
                sql.addSql("\nORDER BY ")

                var sep = ""

                orderBy.forEach {
                    sql.addSql(sep)

                    val orderKey = it.toOrderKey()

                    compileExpr(orderKey.expr)

                    sql.addSql(" ${orderKey.order.sql}")

                    sep = ", "
                }
            }

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