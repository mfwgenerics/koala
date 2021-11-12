package io.koalaql.dsl

import io.koalaql.Assignment
import io.koalaql.expr.Expr
import io.koalaql.expr.Reference
import io.koalaql.query.InsertableLabelList
import io.koalaql.query.LabelListOf
import io.koalaql.query.Values
import io.koalaql.values.*

inline fun <T> values(
    source: Sequence<T>,
    references: List<Reference<*>>,
    crossinline writer: ValuesWriter.(T) -> Unit
): Values {
    val columns = LabelListOf(references)

    return Values(columns) {
        val iter = source.iterator()

        object : ValuesIterator {
            override var row: PreLabeledValues = PreLabeledValues(columns)

            override fun next(): Boolean {
                if (!iter.hasNext()) return false

                row.clear()
                row.writer(iter.next())

                return true
            }

            override fun takeRow(): ValuesRow {
                val result = row
                row = PreLabeledValues(columns)
                return result
            }

            override fun close() { }
        }
    }
}

inline fun <T> values(
    source: Sequence<T>,
    crossinline build: ValuesWriter.(T) -> Unit
): Values {
    val rows = arrayListOf<ValuesRow>()

    val columns = arrayListOf<Reference<*>>()
    val columnPositions = hashMapOf<Reference<*>, Int>()

    val labels = InsertableLabelList()

    val writer = object : ValuesWriter {
        private var values = arrayListOf<Expr<*>?>()

        override fun <T : Any> set(reference: Reference<T>, value: Expr<T>) {
            labels.insert(reference)

            val ix = columnPositions.putIfAbsent(reference, columnPositions.size)

            if (ix != null) {
                values[ix] = value
            } else {
                columns.add(reference)
                values.add(value)
            }
        }

        fun next() {
            rows.add(BuiltValues(
                labels, /* it is deliberate that this continues to be mutated after BuiltRow is constructed */
                values
            ))

            val nextValues = ArrayList<Expr<*>?>(columnPositions.size)
            repeat(columnPositions.size) { nextValues.add(null) }

            values = nextValues
        }
    }

    source.forEach {
        writer.build(it)
        writer.next()
    }

    return Values(labels) {
        IteratorToValuesIterator(rows.iterator())
    }
}

inline fun <T> values(
    source: Iterable<T>,
    references: List<Reference<*>>,
    crossinline writer: ValuesWriter.(T) -> Unit
): Values = values(source.asSequence(), references) { writer(it) }

inline fun <T> values(
    source: Iterable<T>,
    crossinline writer: ValuesWriter.(T) -> Unit
): Values = values(source.asSequence()) { writer(it) }

fun values(
    rows: Iterable<ValuesRow>
): Values {
    val labelSet = hashSetOf<Reference<*>>()

    rows.forEach {
        labelSet.addAll(it.columns)
    }

    val columns = LabelListOf(labelSet.toList())

    return Values(columns) {
        IteratorToValuesIterator(rows.iterator())
    }
}

fun values(vararg rows: ValuesRow): Values = values(rows.asList())

private fun <T : Any> Assignment<T>.placeIntoRow(row: ValuesWriter) {
    row[reference] = expr
}

fun rowOf(assignments: List<Assignment<*>>): ValuesRow {
    checkNotNull(assignments.isNotEmpty()) { "rowOf must contain at least one value" }

    /* could be done more efficiently (?) by building labels and row values together */
    val row = PreLabeledValues(LabelListOf(assignments.map { it.reference }))

    assignments.forEach { it.placeIntoRow(row) }

    return row
}

fun rowOf(vararg assignments: Assignment<*>): ValuesRow =
    rowOf(assignments.asList())