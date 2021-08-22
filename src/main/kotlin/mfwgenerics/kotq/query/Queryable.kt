package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltQuery

interface Queryable {
    fun buildQuery(): BuiltQuery
}