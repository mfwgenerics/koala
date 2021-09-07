package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.LiteralAssignment
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Values
import mfwgenerics.kotq.values.*

inline fun <T> values(
    source: Sequence<T>,
    vararg references: Reference<*>,
    crossinline writer: RowWriter.(T) -> Unit
): Values {
    val columns = LabelList(references.asList())

    return Values(columns) {
        var row = PreLabeledRow(columns)

        val iter = source.iterator()

        object : RowIterator, ValuesRow by row {
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

fun values(
    vararg rows: ValuesRow
): Values {
    val labelSet = hashSetOf<Reference<*>>()

    rows.forEach {
        labelSet.addAll(it.labels.values)
    }

    val columns = LabelList(labelSet.toList())

    return Values(columns) {
        val iter = rows.iterator()

        object : RowIterator {
            override val labels: LabelList get() = columns
            lateinit var row: ValuesRow

            override fun next(): Boolean {
                if (!iter.hasNext()) return false

                row = iter.next()

                return true
            }

            override fun consume(): ValuesRow = row

            override fun <T : Any> get(reference: Reference<T>): T? =
                row[reference]
        }
    }
}

fun rowOf(vararg assignments: LiteralAssignment<*>): ValuesRow {
    /* could be done more efficiently (?) by building labels and row values together */
    val row = PreLabeledRow(LabelList(assignments.map { it.reference }))

    assignments.forEach { it.placeIntoRow(row) }

    return row
}