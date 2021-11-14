package io.koalaql.expr

class WhenThen<T : Any, R : Any>(
    val whenExpr: Expr<T>,
    val thenExpr: Expr<R>
)