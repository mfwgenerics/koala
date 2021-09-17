package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.values.RowSequence

interface Queryable: PerformableQuery {
    fun buildQuery(): BuiltQuery
}