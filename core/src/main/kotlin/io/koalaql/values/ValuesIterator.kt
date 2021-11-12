package io.koalaql.values

interface ValuesIterator {
    /* current row. reference is valid until subsequent call to next */
    val row: ValuesRow

    fun next(): Boolean
}