package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.values.*

interface QueryableOfTwo<A : Any, B : Any>: Queryable<RowWithTwoColumns<A, B>> {
    override fun performWith(ds: BlockingPerformer): RowSequence<RowWithTwoColumns<A, B>> =
        ds.query(buildQuery()).unsafeCastToTwoColumns()
}