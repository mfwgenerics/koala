package io.koalaql.expr

import io.koalaql.dsl.value

class When<T : Any>(
    private val whenExpr: Expr<T>
) {
    fun <R : Any> then(expr: Expr<R>): WhenThen<T, R> =
        WhenThen(whenExpr, expr)

    inline fun <reified R : Any> then(expr: R): WhenThen<T, R> =
        then(value(expr))
}