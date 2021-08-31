package mfwgenerics.kotq.values

import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.query.built.BuiltValuesQuery

interface RowSequence: Sequence<ValuesRow>, Subqueryable {
    val columns: LabelList

    fun rowIterator(): RowIterator

    override fun iterator(): Iterator<ValuesRow> =
        RowIteratorToIterator(rowIterator())

    override fun buildQuery(): BuiltSubquery =
        BuiltValuesQuery(this)
}