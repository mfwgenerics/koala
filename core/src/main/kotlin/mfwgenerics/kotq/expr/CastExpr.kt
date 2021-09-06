package mfwgenerics.kotq.expr

import mfwgenerics.kotq.data.DataType

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: DataType<T>
): Expr<T> {
}