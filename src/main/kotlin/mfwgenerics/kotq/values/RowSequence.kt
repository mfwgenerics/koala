package mfwgenerics.kotq.values

import mfwgenerics.kotq.Queryable
import mfwgenerics.kotq.query.BuiltQuery
import mfwgenerics.kotq.query.BuiltValuesQuery
import mfwgenerics.kotq.query.LabelList

interface RowSequence: Sequence<ValuesRow>, Queryable {
    val columns: LabelList

    fun rowIterator(): RowIterator

    override fun iterator(): Iterator<ValuesRow> =
        RowIteratorToIterator(rowIterator())

    override fun buildQuery(): BuiltQuery =
        BuiltValuesQuery(this)
}