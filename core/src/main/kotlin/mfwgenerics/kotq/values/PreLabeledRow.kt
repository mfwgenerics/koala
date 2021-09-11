package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

class PreLabeledRow(
    val labels: LabelList,
): ValuesRow(), RowWriter {
    override val columns: Collection<Reference<*>> get() = labels.values
    private val values = arrayOfNulls<Any>(labels.values.size)

    fun clear() {
        repeat(values.size) { values[it] = null }
    }

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = labels.positionOf(reference) ?: return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }

    override fun <T : Any> set(reference: Reference<T>, value: T?) {
        val ix = checkNotNull(labels.positionOf(reference)) {
            "$reference not representable in $labels"
        }

        values[ix] = value
    }
}