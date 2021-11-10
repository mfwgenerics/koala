package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithThreeColumns
import io.koalaql.values.unsafeCastToThreeColumns

interface QueryableOfThree<A : Any, B : Any, C : Any>: QueryableUnionOperand<RowWithThreeColumns<A, B, C>> {
    override fun performWith(ds: BlockingPerformer): RowSequence<RowWithThreeColumns<A, B, C>> =
        ds.query(BuiltFullQuery.from(this)).unsafeCastToThreeColumns()
}