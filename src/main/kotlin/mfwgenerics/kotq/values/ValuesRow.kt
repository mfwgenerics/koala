package mfwgenerics.kotq.values

import mfwgenerics.kotq.dsl.LabelList
import mfwgenerics.kotq.expr.Reference

interface ValuesRow {
    val labels: LabelList

    operator fun <T : Any> get(reference: Reference<T>): T?
}