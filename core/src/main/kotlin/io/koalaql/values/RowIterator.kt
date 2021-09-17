package io.koalaql.values

import io.koalaql.expr.Reference

interface RowIterator {
    val columns: Collection<Reference<*>>

    /* current row. reference is valid until subsequent call to next */
    val row: ValuesRow

    /* returns a permanent reference to the current row.
       can only be called once per `next()` and invalidates `row` */
    fun takeRow(): ValuesRow

    fun next(): Boolean

    /* optional to call - resources will be cleaned up on connection/transaction close */
    fun close()
}