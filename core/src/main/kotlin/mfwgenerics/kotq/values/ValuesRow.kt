package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

interface ValuesRow {
    val columns: Collection<Reference<*>>

    operator fun <T : Any> get(reference: Reference<T>): T?
}