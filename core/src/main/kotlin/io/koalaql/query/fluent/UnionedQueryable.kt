package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface UnionedQueryable: Queryable<ResultRow> {
    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
        ds.query(BuiltFullQuery.from(this))
}