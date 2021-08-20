package mfwgenerics.kotq.values

import mfwgenerics.kotq.dsl.LabelList
import mfwgenerics.kotq.dsl.Queryable
import mfwgenerics.kotq.query.built.BuiltQuery
import mfwgenerics.kotq.query.built.BuiltValuesQuery

interface RowSequence: Sequence<ValuesRow>, Queryable {
    val columns: LabelList

    fun rowIterator(): RowIterator

    override fun iterator(): Iterator<ValuesRow> =
        RowIteratorToIterator(rowIterator())

    override fun buildQuery(): BuiltQuery =
        BuiltValuesQuery(this)
}