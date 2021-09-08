package mfwgenerics.kotq.expr

import mfwgenerics.kotq.data.UnmappedDataType

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: UnmappedDataType<T>
): Expr<T> {
}