package io.koalaql.values

class RowIteratorToIterator<T>(
    private val rows: RowIterator<T>
): Iterator<T> {
    private object Initial

    private var currentRow: Any? = Initial

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

    override fun next(): T {
        val result = checkNotNull(currentRow)
            { "no next row" }

        fetch()

        @Suppress("unchecked_cast")
        return result as T
    }
}