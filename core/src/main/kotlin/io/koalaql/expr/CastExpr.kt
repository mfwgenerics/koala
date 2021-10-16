package io.koalaql.expr

import io.koalaql.ddl.UnmappedDataType

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: UnmappedDataType<T>
): Expr<T> {
}