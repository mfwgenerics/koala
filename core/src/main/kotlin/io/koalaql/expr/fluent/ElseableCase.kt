package io.koalaql.expr.fluent

import io.koalaql.expr.BuiltCaseExpr
import io.koalaql.expr.CaseBuilder
import io.koalaql.expr.Expr

interface ElseableCase<T : Any>: EndableCase<T> {
    private class Else<T : Any>(
        val lhs: ElseableCase<*>,
        val expr: Expr<T>
    ): EndableCase<T> {
        override fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder? {
            elseExpr = expr
            return lhs
        }
    }

    infix fun else_(expr: Expr<T>): EndableCase<T> = Else(this, expr)
}