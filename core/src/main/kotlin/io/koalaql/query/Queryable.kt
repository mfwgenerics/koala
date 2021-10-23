package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface Queryable: PerformableBlocking<RowSequence<ResultRow>> {
    fun buildQuery(): BuiltQuery

    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
        ds.query(buildQuery())
}