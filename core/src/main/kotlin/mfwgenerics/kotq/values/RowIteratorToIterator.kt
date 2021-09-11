package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.Reference

class RowIteratorToIterator(
    private val rows: RowIterator
): Iterator<ValuesRow> {
    private object Initial: ValuesRow() {
        override val columns: Collection<Reference<*>> get() = error("not implemented")
        override fun <T : Any> getOrNull(reference: Reference<T>): T? { error("not implemented") }
    }

    private var currentRow: ValuesRow? = Initial

    private fun fetch() {
        val hadNext = rows.next()

        currentRow = if (hadNext) {
            rows.takeRow()
        } else {
            rows.close()
            null
        }
    }

    override fun hasNext(): Boolean {
        if (currentRow === Initial) fetch()

        return currentRow != null
    }

    override fun next(): ValuesRow {
        val result = checkNotNull(currentRow)
            { "no next row" }

        fetch()

        return result
    }
}