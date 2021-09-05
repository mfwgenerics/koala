package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltQuery
import mfwgenerics.kotq.values.RowSequence

interface Queryable: Performable<RowSequence> {
    fun buildQuery(): BuiltQuery
}