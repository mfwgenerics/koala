package io.koalaql.expr

interface AsReference<T : Any>: Expr<T> {
    fun asReference(): Reference<T>
}