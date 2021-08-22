package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

class PreLabeledRow(
    override val labels: LabelList,
): ValuesRow, RowWriter {
    private val values = arrayOfNulls<Any>(labels.values.size)

    fun clear() {
        repeat(values.size) { values[it] = null }
    }

    override operator fun <T : Any> get(reference: Reference<T>): T? {
        val ix = labels.positionOf(reference) ?: return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }

    override fun <T : Any> value(reference: Reference<T>, value: T?) {
        val ix = checkNotNull(labels.positionOf(reference)) {
            "$reference not representable in $labels"
        }

        values[ix] = value
    }
}