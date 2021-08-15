package mfwgenerics.kotq.dialect.mysql

import mfwgenerics.kotq.*
import mfwgenerics.kotq.dialect.SqlDialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.SqlText
import mfwgenerics.kotq.sql.SqlTextBuilder
import java.util.function.BinaryOperator

class MysqlDialect: SqlDialect {
    private fun compileExpr(expr: Expr<*>, sql: SqlTextBuilder) {
        when (expr) {
            is And -> TODO()
            is Comparison<*> -> TODO()
            is Constant -> TODO()
            is Reference -> TODO()
        }
    }

    private fun compileQueryWhere(query: QueryWhere, sql: SqlTextBuilder) {
        val relation = query.relation!!.relation

        when (relation) {
            is Table -> sql.addSql(relation.name)
            is Subquery -> compileSelect(relation.of.buildQuery(), sql)
        }

        sql.addSql("\n")

        query.joins.forEach { join ->
            sql.addSql(join.type.sql)
            sql.addSql(" ON ")
            compileExpr(join.on, sql)
        }

        query.where?.let {
            sql.addSql("\nWHERE ")
            compileExpr(it, sql)
        }
    }

    private fun compileSelectBody(body: SelectBody, sql: SqlTextBuilder) {
        compileQueryWhere(body.where, sql)

        body.groupBy.takeIf { it.isNotEmpty() }?.let { groupBy ->
            sql.addSql("\nGROUP BY ")

            var sep = ""

            groupBy.forEach {
                sql.addSql(sep)
                compileExpr(it, sql)
                sep = ", "
            }
        }

        body.having?.let {
            sql.addSql("\nHAVING ")
            compileExpr(it, sql)
        }
    }

    private fun compileSelect(select: SelectQuery, sql: SqlTextBuilder) {
        sql.addSql("SELECT")

        var sep = ""

        select.selected.forEach { _ ->
            sql.addSql("$sep\n")
            sep = ", "
        }

        sql.addSql("\nFROM ")

        compileSelectBody(select.body, sql)

        select.orderBy.takeIf { it.isNotEmpty() }?.let { orderBy ->
            sql.addSql("\nORDER BY ")

            var sep = ""

            orderBy.forEach {
                sql.addSql(sep)

                val orderKey = it.toOrderKey()

                compileExpr(orderKey.expr, sql)

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

    override fun compile(statement: Statement): SqlText {
        val sql = SqlTextBuilder()

        if (statement is SelectQuery) {
            compileSelect(statement, sql)
        }

        return sql.toSql()
    }
}