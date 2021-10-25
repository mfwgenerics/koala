package io.koalaql.values

class IteratorToRowIterator<T : Any>(
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
