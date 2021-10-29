package io.koalaql.values

import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class PreLabeledResults(
    override val columns: LabelList
): RawResultRow {
    private val values = arrayOfNulls<Any>(columns.size)

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = columns.positionOf(reference) ?: return null

        @Suppress("unchecked_cast")
        return values[ix] as T?
    }

    override fun get(ix: Int): Any? = values[ix]

    fun <T : Any> set(reference: Reference<T>, value: T?) {
        val ix = checkNotNull(columns.positionOf(reference)) {
            "$reference not representable in $columns"
        }

        values[ix] = value
    }

    override fun toString(): String  =
        columns.asSequence().map { "$it=${getOrNull(it)}" }.joinToString()
}