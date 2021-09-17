package io.koalaql.expr

class ExprListExpr<T : Any>(
    val exprs: Collection<Expr<T>>
): QuasiExpr {
}