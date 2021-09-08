package mfwgenerics.kotq.values

interface RowIterator: ValuesRow {
    fun next(): Boolean
    fun consume(): ValuesRow

    /* optional to call - resources will be cleaned up on connection/transaction close */
    fun close()
}