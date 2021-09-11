package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

class IteratorToRowIterator(
    val labels: LabelList,
    val iter: Iterator<ValuesRow>
): RowIterator {
    override val columns: Collection<Reference<*>> get() = labels.values

    lateinit var row: ValuesRow

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }

    override fun consume(): ValuesRow = row

    override fun <T : Any> getOrNull(reference: Reference<T>): T? =
        row.getOrNull(reference)

    override fun close() { }
}
