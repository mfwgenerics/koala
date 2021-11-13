package io.koalaql.expr

class BetweenExpr<T : Any>(
    val value: Expr<T>,
    val low: Expr<T>,
    val high: Expr<T>
): Expr<Boolean>