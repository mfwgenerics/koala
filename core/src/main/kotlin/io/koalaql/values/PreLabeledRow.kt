package io.koalaql.values

import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class PreLabeledRow(
    override val columns: LabelList
): ValuesRow(), RowWriter {
    private val values = arrayOfNulls<Any>(columns.size)

    fun clear() {
        repeat(values.size) { values[it] = null }
    }

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = columns.positionOf(reference) ?: return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }

    override fun <T : Any> set(reference: Reference<T>, value: T?) {
        val ix = checkNotNull(columns.positionOf(reference)) {
            "$reference not representable in $columns"
        }

        values[ix] = value
    }
}