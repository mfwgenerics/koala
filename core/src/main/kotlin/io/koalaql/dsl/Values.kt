package io.koalaql.dsl

import io.koalaql.LiteralAssignment
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.query.Values
import io.koalaql.values.*

inline fun <T> values(
    source: Sequence<T>,
    references: List<Reference<*>>,
    crossinline writer: RowWriter.(T) -> Unit
): Values {
    val columns = LabelList(references)

    return Values(columns) {
        val iter = source.iterator()

        object : RowIterator {
            override val columns: Collection<Reference<*>> get() = columns.values

            override var row: PreLabeledRow = PreLabeledRow(columns)

            override fun next(): Boolean {
                if (!iter.hasNext()) return false

                row.clear()
                row.writer(iter.next())

                return true
            }

            override fun takeRow(): ValuesRow {
                val result = row
                row = PreLabeledRow(columns)
                return result
            }

            override fun close() { }
        }
    }
}

inline fun <T> values(
    source: Sequence<T>,
    crossinline build: RowWriter.(T) -> Unit
): Values {
    val rows = arrayListOf<ValuesRow>()

    val columns = arrayListOf<Reference<*>>()
    val columnPositions = hashMapOf<Reference<*>, Int>()

    val writer = object : RowWriter {
        private var values = arrayListOf<Any?>()

        override fun <T : Any> set(reference: Reference<T>, value: T?) {
            val ix = columnPositions.putIfAbsent(reference, columnPositions.size)

            if (ix != null) {
                values[ix] = value
            } else {
                columns.add(reference)
                values.add(value)
            }
        }

        fun next() {
            rows.add(BuiltRow(
                columnPositions, /* it is deliberate that this continues to be mutated after BuiltRow is constructed */
                values
            ))

            val nextValues = ArrayList<Any?>(columnPositions.size)
            repeat(columnPositions.size) { nextValues.add(null) }

            values = nextValues
        }
    }

    source.forEach {
        writer.build(it)
        writer.next()
    }

    val labels = LabelList(
        columns,
        columnPositions
    )

    check (labels.values.isNotEmpty())
        { "values requires at least one value with at least one assignment" }

    return Values(labels) {
        IteratorToRowIterator(labels, rows.iterator())
    }
}

inline fun <T> values(
    source: Iterable<T>,
    references: List<Reference<*>>,
    crossinline writer: RowWriter.(T) -> Unit
): Values = values(source.asSequence(), references) { writer(it) }

inline fun <T> values(
    source: Iterable<T>,
    crossinline writer: RowWriter.(T) -> Unit
): Values = values(source.asSequence()) { writer(it) }

fun values(
    rows: Iterable<ValuesRow>
): Values {
    val labelSet = hashSetOf<Reference<*>>()

    rows.forEach {
        labelSet.addAll(it.columns)
    }

    check (labelSet.isNotEmpty()) { "empty values" }

    val columns = LabelList(labelSet.toList())

    return Values(columns) {
        IteratorToRowIterator(columns, rows.iterator())
    }
}

fun values(vararg rows: ValuesRow): Values = values(rows.asList())

fun rowOf(assignments: List<LiteralAssignment<*>>): ValuesRow {
    checkNotNull(assignments.isNotEmpty()) { "rowOf must contain at least one value" }

    /* could be done more efficiently (?) by building labels and row values together */
    val row = PreLabeledRow(LabelList(assignments.map { it.reference }))

    assignments.forEach { it.placeIntoRow(row) }

    return row
}

fun rowOf(vararg assignments: LiteralAssignment<*>): ValuesRow =
    rowOf(assignments.asList())

fun rowOfNotNull(vararg assignments: LiteralAssignment<*>?): ValuesRow =
    rowOf(assignments.mapNotNull { it })