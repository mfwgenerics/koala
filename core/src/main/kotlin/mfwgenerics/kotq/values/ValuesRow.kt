package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

interface ValuesRow {
    val columns: Collection<Reference<*>>

    fun <T : Any> getOrNull(reference: Reference<T>): T?

    operator fun <T : Any, R : T?> get(reference: Reference<T>): R {
        @Suppress("unchecked_cast")
        return getOrNull(reference) as R
    }
}