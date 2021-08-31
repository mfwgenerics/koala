package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltSubquery

interface Subqueryable: Queryable {
    override fun buildQuery(): BuiltSubquery

    fun subquery() = Subquery(buildQuery())
}