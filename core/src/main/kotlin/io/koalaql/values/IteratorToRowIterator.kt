package io.koalaql.values

import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class IteratorToRowIterator(
    val labels: LabelList,
    val iter: Iterator<ValuesRow>
): RowIterator {
    override val columns: Collection<Reference<*>> get() = labels.values

    override lateinit var row: ValuesRow

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }

    override fun takeRow(): ValuesRow = row

    override fun close() { }
}
