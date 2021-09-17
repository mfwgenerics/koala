package io.koalaql.values

import io.koalaql.query.LabelList

interface RowSequence: Sequence<ValuesRow> {
    val columns: LabelList

    fun rowIterator(): RowIterator

    override fun iterator(): Iterator<ValuesRow> =
        RowIteratorToIterator(rowIterator())
}