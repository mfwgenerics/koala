package io.koalaql.query

class ReversedList<T>(
    private val values: ArrayList<T> = arrayListOf()
): List<T> by (values as List<T>).asReversed() {
    /* upcast to get correct .asReversed() ^ */
    fun add(element: T) = values.add(element)
}