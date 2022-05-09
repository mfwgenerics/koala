package io.koalaql.sql

import io.koalaql.ddl.UnmappedDataType
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.built.BuiltAggregatedExpr
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.window.built.BuiltWindow

interface Compiler {
    fun addLiteral(builder: ScopedSqlBuilder, value: Literal<*>?) = builder.output.addLiteral(value)

    fun excluded(builder: ScopedSqlBuilder, reference: Reference<*>)

    fun <T : Any> reference(builder: ScopedSqlBuilder, emitParens: Boolean, value: Reference<T>)
    fun subquery(builder: ScopedSqlBuilder, emitParens: Boolean, subquery: BuiltSubquery)

    fun aggregatable(builder: ScopedSqlBuilder, emitParens: Boolean, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(builder: ScopedSqlBuilder, to: UnmappedDataType<T>)

    fun window(builder: ScopedSqlBuilder, window: BuiltWindow)

    fun compileExpr(
        builder: ScopedSqlBuilder,
        expr: QuasiExpr,
        emitParens: Boolean
    ): Unit = with(builder) {
        val forceExhaustiveWhen = when (expr) {
            is AggregatedExpr<*> -> {
                val aggregated = BuiltAggregatedExpr.from(expr)

                addSql(aggregated.expr.type.sql)
                parenthesize {
                    if (aggregated.expr.args.isNotEmpty()) {
                        prefix("", ", ").forEach(aggregated.expr.args) {
                            compiler.aggregatable(this, false, it)
                        }
                    } else if (aggregated.expr.type == OperationType.COUNT) {
                        addSql("*")
                    }
                }

                aggregated.filter?.let { filter ->
                    addSql(" FILTER(WHERE ")

                    compiler.compileExpr(this@with, filter, false)

                    addSql(")")
                }

                aggregated.over?.let { window ->
                    addSql(" OVER (")
                    compiler.window(this, window)
                    addSql(")")
                }
            }
            is CastExpr<*> -> {
                addSql("CAST")
                parenthesize {
                    compiler.compileExpr(this@with, expr.of, false)
                    addSql(" AS ")
                    compiler.dataTypeForCast(this, expr.type)
                }
            }
            is ComparedQuery<*> -> {
                addSql(expr.type.sql)
                compiler.subquery(this, false, expr.subquery)
            }
            is ExprListExpr<*> -> {
                parenthesize {
                    prefix("", ", ").forEach(expr.exprs) {
                        compiler.compileExpr(this@with, it, false)
                    }
                }
            }
            is Literal<*> -> compiler.addLiteral(this, expr)
            is OperationExpr<*> -> {
                when (expr.type.fixity) {
                    OperationFixity.NAME -> addSql(expr.type.sql)
                    OperationFixity.PREFIX -> parenthesize(emitParens) {
                        addSql(expr.type.sql)
                        addSql(" ")

                        compiler.compileExpr(this@with, expr.args.single(), false)
                    }
                    OperationFixity.POSTFIX -> parenthesize(emitParens) {
                        compiler.compileExpr(this@with, expr.args.single(), false)
                        addSql(" ")
                        addSql(expr.type.sql)
                    }
                    OperationFixity.INFIX -> parenthesize(emitParens) {
                        prefix("", " ${expr.type.sql} ").forEach(expr.args) {
                            compiler.compileExpr(this@with, it, true)
                        }
                    }
                    OperationFixity.APPLY -> {
                        addSql(expr.type.sql)
                        parenthesize {
                            prefix("", ", ").forEach(expr.args) {
                                compiler.compileExpr(this@with, it, false)
                            }
                        }
                    }
                }
            }
            is AsReference<*> -> {
                val reference = expr.asReference()
                val excluded = reference.excludedReference()

                if (excluded != null) {
                    compiler.excluded(this, excluded)
                } else {
                    compiler.reference(this, false, reference)
                }
            }
            is SubqueryQuasiExpr -> {
                compiler.subquery(this, false, expr.query)
            }
            is ExprQueryable<*> -> {
                compiler.subquery(this, false, with (expr) { BuilderContext.buildQuery() })
            }
            is BuiltCaseExpr<*> -> parenthesize(emitParens) {
                addSql("CASE")

                expr.onExpr?.let {
                    addSql(" ")
                    compiler.compileExpr(this@with, it, true)
                }

                expr.whens.forEach { whenThen ->
                    addSql("\nWHEN ")
                    compiler.compileExpr(this@with, whenThen.whenExpr, false)
                    addSql(" THEN ")
                    compiler.compileExpr(this@with, whenThen.thenExpr, true)
                }

                expr.elseExpr?.let {
                    addSql("\nELSE ")
                    compiler.compileExpr(this@with, it, false)
                }

                addSql("\nEND")
            }
            is RawExpr<*> -> {
                val build = expr.build

                object : RawSqlBuilder {
                    override fun sql(value: String) { addSql(value) }
                    override fun expr(expr: QuasiExpr) { compiler.compileExpr(this@with, expr, true) }
                }.build()
            }
            is BetweenExpr<*> -> {
                compiler.compileExpr(this@with, expr.value, false)
                addSql(" BETWEEN ")
                compiler.compileExpr(this@with, expr.low, true)
                addSql(" AND ")
                compiler.compileExpr(this@with, expr.high, true)
            }
        }
    }
}