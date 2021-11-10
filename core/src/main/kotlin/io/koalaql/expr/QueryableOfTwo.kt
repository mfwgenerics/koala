package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithTwoColumns
import io.koalaql.values.unsafeCastToTwoColumns

interface QueryableOfTwo<A : Any, B : Any>: QueryableUnionOperand<RowWithTwoColumns<A, B>> {
    override fun performWith(ds: BlockingPerformer): RowSequence<RowWithTwoColumns<A, B>> =
        ds.query(BuiltFullQuery.from(this)).unsafeCastToTwoColumns()
}