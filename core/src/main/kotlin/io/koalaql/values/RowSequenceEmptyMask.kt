package io.koalaql.values

import io.koalaql.expr.Reference

class RowSequenceEmptyMask(
    private val of: RowSequence<*>
): RowSequence<EmptyRow> {
    override val columns: List<Reference<*>> = emptyList()

    private class Iterator(
        val of: RowIterator<*>
    ): RowIterator<EmptyRow> {
        override val row: EmptyRow = EmptyRow
        override fun takeRow(): EmptyRow = EmptyRow

        override fun next(): Boolean = of.next()
        override fun close() = of.close()
    }

    override fun rowIterator(): RowIterator<EmptyRow> =
        Iterator(of.rowIterator())
}