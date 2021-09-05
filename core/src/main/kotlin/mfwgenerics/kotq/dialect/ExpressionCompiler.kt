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

    fun <T : Any> reference(emitParens: Boolean, value: Reference<T>)
    fun subquery(emitParens: Boolean, subquery: BuiltSubquery)

    fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(to: DataType<T>)

    fun <T : Any> cast(emitParens: Boolean, of: Expr<*>, to: DataType<T>) {
        sql.addSql("CAST")
        sql.parenthesize {
            of.compile(false, this)
            sql.addSql(" AS ")
            dataTypeForCast(to)
        }
    }

    fun window(window: BuiltWindow)

    fun operation(emitParens: Boolean, type: OperationType, args: Collection<QuasiExpr>) {
        when (type.fixity) {
            OperationFixity.PREFIX -> {
                sql.parenthesize(emitParens) {
                    sql.addSql(type.sql)
                    sql.addSql(" ")

                    args.single().compile(false, this@ExpressionCompiler)
                }
            }
            OperationFixity.POSTFIX -> {
                sql.parenthesize(emitParens) {
                    args.single().compile(false, this@ExpressionCompiler)
                    sql.addSql(" ")
                    sql.addSql(type.sql)
                }
            }
            OperationFixity.INFIX -> {
                sql.parenthesize(emitParens) {
                    sql.prefix("", " ${type.sql} ").forEach(args) {
                        it.compile(true, this@ExpressionCompiler)
                    }
                }
            }
            OperationFixity.APPLY -> {
                sql.addSql(type.sql)
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(args) {
                        it.compile(false, this@ExpressionCompiler)
                    }
                }
            }
        }
    }

    fun <T : Any> literal(emitParens: Boolean, value: Literal<T>)
        { sql.addLiteral(value) }

    fun aggregated(emitParens: Boolean, aggregated: BuiltAggregatedExpr) {
        sql.addSql(aggregated.expr.type.sql)
        sql.parenthesize {
            sql.prefix("", ", ").forEach(aggregated.expr.args) {
                aggregatable(false, it)
            }
        }

        aggregated.filter?.let { filter ->
            sql.addSql(" FILTER(WHERE ")

            filter.compile(false, this@ExpressionCompiler)
            sql.addSql(")")
        }

        aggregated.over?.let { window ->
            sql.addSql(" OVER (")
            window(window)
            sql.addSql(")")
        }
    }

    fun <T : Any> listExpr(emitParens: Boolean, exprs: Collection<Expr<T>>) {
        sql.parenthesize {
            sql.prefix("", ", ").forEach(exprs) {
                it.compile(false, this)
            }
        }
    }

    fun compared(emitParens: Boolean, type: ComparedQueryType, subquery: BuiltSubquery) {
        sql.addSql(type)
        subquery(false, subquery)
    }
}