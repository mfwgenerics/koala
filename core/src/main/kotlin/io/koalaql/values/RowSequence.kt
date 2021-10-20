package io.koalaql.values

import io.koalaql.query.LabelList

interface RowSequence<T>: Sequence<T> {
    val columns: LabelList

    fun rowIterator(): RowIterator<T>

    override fun iterator(): Iterator<T> =
        RowIteratorToIterator(rowIterator())
}