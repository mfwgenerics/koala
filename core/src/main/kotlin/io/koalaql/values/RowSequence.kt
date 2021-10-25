package io.koalaql.values

import io.koalaql.expr.Reference

interface RowSequence<T>: Sequence<T> {
    val columns: List<Reference<*>>

    fun rowIterator(): RowIterator<T>

    override fun iterator(): Iterator<T> =
        RowIteratorToIterator(rowIterator())
}