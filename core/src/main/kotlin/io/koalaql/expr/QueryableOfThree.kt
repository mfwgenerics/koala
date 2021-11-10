package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.WithType
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith
import io.koalaql.query.built.QueryBuilder
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.*

interface QueryableOfThree<A : Any, B : Any, C : Any>: QueryableUnionOperand<RowWithThreeColumns<A, B, C>> {
    override fun perform(ds: BlockingPerformer): RowSequence<RowWithThreeColumns<A, B, C>> =
        ds.query(BuiltQuery.from(this)).unsafeCastToThreeColumns()

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<RowWithThreeColumns<A, B, C>> {
        override fun perform(ds: BlockingPerformer): RowSequence<RowWithThreeColumns<A, B, C>> =
            ds.query(BuiltQuery.from(this)).unsafeCastToThreeColumns()

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@QueryableOfThree
        }
    }
}