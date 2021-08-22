package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList

interface ValuesRow {
    val labels: LabelList

    operator fun <T : Any> get(reference: Reference<T>): T?
}