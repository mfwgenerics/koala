package io.koalaql.values

import io.koalaql.expr.Reference

interface ValuesSequence {
    val columns: List<Reference<*>>

    fun valuesIterator(): RowIterator<ValuesRow>
}