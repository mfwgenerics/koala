package mfwgenerics.kotq.expr

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: DataType<T>
): Expr<T>