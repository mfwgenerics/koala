package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.expr.built.BuiltAggregatedExpr
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.sql.SqlTextBuilder
import mfwgenerics.kotq.window.built.BuiltWindow

interface ExpressionCompiler {
    val sql: SqlTextBuilder

    fun <T : Any> reference(context: ExpressionContext, value: Reference<T>)
    fun subquery(context: ExpressionContext, subquery: BuiltSubquery)

    fun aggregatable(context: ExpressionContext, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(to: DataType<T>)

    fun <T : Any> cast(context: ExpressionContext, of: Expr<*>, to: DataType<T>) {
        sql.addSql("CAST")
        sql.parenthesize {
            of.compile(context.copy(needsParens = false), this)
            sql.addSql(" AS ")
            dataTypeForCast(to)
        }
    }

    fun window(out: SqlTextBuilder, window: BuiltWindow)

    fun operation(context: ExpressionContext, type: OperationType, args: Collection<QuasiExpr>) {
        when (type.fixity) {
            OperationFixity.PREFIX -> {
                sql.parenthesize(context.needsParens) {
                    sql.addSql(type.sql)
                    sql.addSql(" ")

                    args.single().compile(context.copy(needsParens = false), this@ExpressionCompiler)
                }
            }
            OperationFixity.POSTFIX -> {
                sql.parenthesize(context.needsParens) {
                    args.single().compile(context.copy(needsParens = false), this@ExpressionCompiler)
                    sql.addSql(" ")
                    sql.addSql(type.sql)
                }
            }
            OperationFixity.INFIX -> {
                sql.parenthesize(context.needsParens) {
                    sql.prefix("", " ${type.sql} ").forEach(args) {
                        it.compile(context.copy(needsParens = true), this@ExpressionCompiler)
                    }
                }
            }
            OperationFixity.APPLY -> {
                sql.addSql(type.sql)
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(args) {
                        it.compile(context.copy(needsParens = false), this@ExpressionCompiler)
                    }
                }
            }
        }
    }

    fun <T : Any> literal(context: ExpressionContext, value: Literal<T>)
        { sql.addLiteral(value) }

    fun aggregated(context: ExpressionContext, aggregated: BuiltAggregatedExpr) {
        sql.addSql(aggregated.expr.type.sql)
        sql.parenthesize {
            sql.prefix("", ", ").forEach(aggregated.expr.args) {
                aggregatable(context, it)
            }
        }

        aggregated.filter?.let { filter ->
            sql.addSql(" FILTER(WHERE ")

            filter.compile(context.copy(needsParens = false), this@ExpressionCompiler)
            sql.addSql(")")
        }

        aggregated.over?.let { window ->
            sql.addSql(" OVER (")
            window(sql, window)
            sql.addSql(")")
        }
    }

    fun <T : Any> listExpr(context: ExpressionContext, exprs: Collection<Expr<T>>) {
        sql.parenthesize {
            sql.prefix("", ", ").forEach(exprs) {
                it.compile(context, this)
            }
        }
    }

    fun compared(context: ExpressionContext, type: ComparedQueryType, subquery: BuiltSubquery) {
        sql.addSql(type)
        subquery(context.copy(needsParens = false), subquery)
    }
}