package io.koalaql.expr

class When<T : Any>(
    private val whenExpr: Expr<T>
) {
    fun <R : Any> then(expr: Expr<R>): WhenThen<T, R> =
        WhenThen(whenExpr, expr)
}