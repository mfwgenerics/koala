package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

abstract class ValuesRow {
    abstract val columns: Collection<Reference<*>>

    abstract fun <T : Any> getOrNull(reference: Reference<T>): T?

    fun <T : Any> getValue(reference: Reference<T>): T =
        checkNotNull(getOrNull(reference)) { "expected non-null $reference" }

    /* reified R so nullability cast can be checked */
    inline operator fun <T : Any, reified R : T?> get(reference: Reference<T>): R {
        return getOrNull(reference) as R
    }

    override fun toString(): String =
        columns.asSequence().map { "$it=${getOrNull(it)}" }.joinToString()
}