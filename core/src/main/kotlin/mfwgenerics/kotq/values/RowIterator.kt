package mfwgenerics.kotq.values

interface RowIterator: ValuesRow {
    fun next(): Boolean
    fun consume(): ValuesRow
}