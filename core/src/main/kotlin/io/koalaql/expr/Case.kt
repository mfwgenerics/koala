package io.koalaql.expr

import io.koalaql.expr.fluent.EndableCase

class Case<S : Any>(
    private val on: Expr<S>? = null
): CaseBuilder {
    private class Else<T : Any>(
        private val lhs: Case<*>,
        private val expr: Expr<T>
    ): EndableCase<T> {
        override fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder? {
            elseExpr = expr
            return lhs
        }
    }

    infix fun when_(expr: Expr<S>): CaseWhen<S> = CaseWhen(this, expr)
    infix fun <T : Any> else_(expr: Expr<T>): EndableCase<T> = Else(this, expr)

    fun <T : Any> end(): Expr<T> = BuiltCaseExpr.from(this)

    override fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder? {
        onExpr = on
        return null
    }
}