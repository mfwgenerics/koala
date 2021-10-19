package io.koalaql.expr.fluent

import io.koalaql.expr.BuiltCaseExpr
import io.koalaql.expr.CaseBuilder
import io.koalaql.expr.CaseWhenThen
import io.koalaql.expr.Expr

interface WhenableCase<S : Any, T : Any>: ElseableCase<T> {
    private class When<S : Any, T : Any>(
        private val lhs: WhenableCase<S, T>,
        private val whenExpr: Expr<S>
    ): ThenableCase<S, T> {
        override fun <R : T> then(expr: Expr<R>): WhenableCase<S, R> {
            return object : WhenableCase<S, R> {
                override fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder {
                    whens.addFirst(CaseWhenThen(whenExpr, expr))
                    return lhs
                }
            }
        }
    }

    infix fun when_(expr: Expr<S>): ThenableCase<S, T> =
        When(this, expr)
}