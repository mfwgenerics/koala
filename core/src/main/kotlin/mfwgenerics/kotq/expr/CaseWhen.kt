package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dsl.value

class CaseWhen<T : Any>(
    private val matcher: Expr<T>
) {
    infix fun <R : Any> then(expr: Expr<R>): CaseWhenThen<T, R> =
        CaseWhenThen(matcher, expr)

    inline infix fun <reified R : Any> then(expr: R): CaseWhenThen<T, R> =
        then(value(expr))
}