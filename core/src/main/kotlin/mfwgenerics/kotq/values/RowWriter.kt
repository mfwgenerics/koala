package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference

interface RowWriter {
    fun <T : Any> set(reference: Reference<T>, value: T?)
}