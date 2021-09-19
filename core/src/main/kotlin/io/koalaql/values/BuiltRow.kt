package io.koalaql.values

import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class BuiltRow(
    override val columns: LabelList,
    private val values: List<Any?>
): ValuesRow() {
    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = columns.positionOf(reference) ?: return null
        if (ix >= values.size) return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }
}