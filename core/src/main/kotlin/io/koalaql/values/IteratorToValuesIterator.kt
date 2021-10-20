package io.koalaql.values

import io.koalaql.query.LabelList

class IteratorToValuesIterator(
    override val columns: LabelList,
    val iter: Iterator<ValuesRow>
): RowIterator<ValuesRow> {
    override lateinit var row: ValuesRow

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }

    override fun takeRow(): ValuesRow = row

    override fun close() { }
}
