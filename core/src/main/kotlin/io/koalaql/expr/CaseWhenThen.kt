package io.koalaql.expr

class CaseWhenThen<T : Any, R : Any>(
    val whenExpr: Expr<T>,
    val thenExpr: Expr<R>
)