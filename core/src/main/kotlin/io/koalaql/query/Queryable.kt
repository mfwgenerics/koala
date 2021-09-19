package io.koalaql.query

import io.koalaql.query.built.BuiltQuery

interface Queryable: PerformableQuery {
    fun buildQuery(): BuiltQuery
}