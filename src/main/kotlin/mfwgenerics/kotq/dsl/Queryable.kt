package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.query.built.BuiltQuery

interface Queryable {
    fun buildQuery(): BuiltQuery

    fun subquery() = Subquery(buildQuery())
}