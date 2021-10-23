package io.koalaql.values

import io.koalaql.query.LabelList

class IteratorToRowIterator<T : Any>(
    override val columns: LabelList,
    val iter: Iterator<T>
): RowIterator<T> {
    override lateinit var row: T

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }

    override fun takeRow(): T = row

    override fun close() { }
}
