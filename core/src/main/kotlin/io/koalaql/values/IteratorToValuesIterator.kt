package io.koalaql.values

class IteratorToValuesIterator(
    val iter: Iterator<ValuesRow>
): ValuesIterator {
    override lateinit var row: ValuesRow

    override fun next(): Boolean {
        if (!iter.hasNext()) return false

        row = iter.next()

        return true
    }
}
