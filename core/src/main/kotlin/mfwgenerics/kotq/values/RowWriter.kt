package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference

interface RowWriter {
    fun <T : Any> value(reference: Reference<T>, value: T?)
}