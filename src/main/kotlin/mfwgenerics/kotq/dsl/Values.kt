package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.LiteralAssignment
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.values.*

inline fun <T> values(
    source: Sequence<T>,
    vararg references: Reference<*>,
    crossinline writer: RowWriter.(T) -> Unit
): RowSequence {
    return object : RowSequence {
        override val columns = LabelList(references.asList())
        var called = false

        override fun rowIterator(): RowIterator {
            called = true

            var row = PreLabeledRow(columns)

            val iter = source.iterator()

            return object : RowIterator, ValuesRow by row {
                override fun next(): Boolean {
                    if (!iter.hasNext()) return false

                    row.clear()
                    row.writer(iter.next())

                    return true
                }

                override fun consume(): ValuesRow {
                    val result = row
                    row = PreLabeledRow(labels)
                    return result
                }
            }
        }
    }
}

fun rowOf(vararg assignments: LiteralAssignment<*>): ValuesRow {
    /* could be done more efficiently (?) by building labels and row values together */
    val row = PreLabeledRow(LabelList(assignments.map { it.reference }))

    assignments.forEach { it.placeIntoRow(row) }

    return row
}