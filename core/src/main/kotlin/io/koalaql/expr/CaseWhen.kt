package io.koalaql.expr

import io.koalaql.expr.fluent.WhenableCase

class CaseWhen<S : Any>(
    private val case: Case<S>,
    private val expr: Expr<S>
) {
    private class Then<S : Any, T : Any>(
        val lhs: CaseWhen<S>,
        val expr: Expr<T>
    ): WhenableCase<S, T> {
        override fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder {
            whens.addFirst(CaseWhenThen(lhs.expr, expr))

            return lhs.case
        }
    }

    infix fun <T : Any> then(expr: Expr<T>): WhenableCase<S, T> = Then(this, expr)
}