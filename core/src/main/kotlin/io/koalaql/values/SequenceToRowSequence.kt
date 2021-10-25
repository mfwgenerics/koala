package io.koalaql.values

import io.koalaql.expr.Reference

class SequenceToRowSequence<T : Any>(
    override val columns: List<Reference<*>>,
    private val sequence: Sequence<T>
): RowSequence<T> {
    override fun rowIterator(): RowIterator<T> =
        IteratorToRowIterator(sequence.iterator())
}