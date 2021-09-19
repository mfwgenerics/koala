package io.koalaql.values

import io.koalaql.query.LabelList

class IteratorToRowIterator(
    override val columns: LabelList,
    val iter: Iterator<ValuesRow>
): RowIterator {
    override lateinit var row: ValuesRow

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }

    override fun takeRow(): ValuesRow = row

    override fun close() { }
}
