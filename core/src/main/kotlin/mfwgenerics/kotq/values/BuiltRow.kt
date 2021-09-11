package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference

class BuiltRow(
    private val columnPositions: Map<Reference<*>, Int>,
    private val values: List<Any?>,
): ValuesRow {
    override val columns: Collection<Reference<*>> get() = columnPositions.keys

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = columnPositions[reference] ?: return null
        if (ix >= values.size) return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }
}