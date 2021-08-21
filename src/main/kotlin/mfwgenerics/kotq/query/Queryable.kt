package mfwgenerics.kotq.query

import mfwgenerics.kotq.dsl.Subquery
import mfwgenerics.kotq.query.built.BuiltQuery

interface Queryable {
    fun buildQuery(): BuiltQuery

    fun subquery() = Subquery(buildQuery())
}