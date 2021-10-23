package io.koalaql.values

import io.koalaql.query.LabelList

class SequenceToRowSequence<T : Any>(
    override val columns: LabelList,
    private val sequence: Sequence<T>
): RowSequence<T> {
    override fun rowIterator(): RowIterator<T> =
        IteratorToRowIterator(columns, sequence.iterator())
}