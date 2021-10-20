package io.koalaql.values

import io.koalaql.query.LabelList

interface RowIterator<T> {
    val columns: LabelList

    /* current row. reference is valid until subsequent call to next */
    val row: T

    /* returns a permanent reference to the current row.
       can only be called once per `next()` and invalidates `row` */
    fun takeRow(): T

    fun next(): Boolean

    /* optional to call - resources will be cleaned up on connection/transaction close */
    fun close()
}