package io.koalaql.expr

import io.koalaql.data.UnmappedDataType

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: UnmappedDataType<T>
): Expr<T> {
}