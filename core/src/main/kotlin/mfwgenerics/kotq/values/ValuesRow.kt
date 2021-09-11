package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

abstract class ValuesRow {
    abstract val columns: Collection<Reference<*>>

    abstract fun <T : Any> getOrNull(reference: Reference<T>): T?

    /* reified R so nullability cast can be checked */
    inline operator fun <T : Any, reified R : T?> get(reference: Reference<T>): R {
        return getOrNull(reference) as R
    }
}