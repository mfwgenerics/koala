package mfwgenerics.kotq.expr

interface AsReference<T : Any>: Expr<T> {
    fun asReference(): Reference<T>
}