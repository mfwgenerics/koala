package io.koalaql.query

import io.koalaql.query.built.BuiltSubquery

interface Subqueryable: Queryable {
    override fun buildQuery(): BuiltSubquery

    fun subquery() = Subquery(buildQuery())
    fun subquery(alias: Alias) = subquery().as_(alias)
}