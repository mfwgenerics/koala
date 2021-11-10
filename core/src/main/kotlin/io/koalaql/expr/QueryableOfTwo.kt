package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.WithType
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith
import io.koalaql.query.built.QueryBuilder
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.*

interface QueryableOfTwo<A : Any, B : Any>: QueryableUnionOperand<RowWithTwoColumns<A, B>> {
    override fun perform(ds: BlockingPerformer): RowSequence<RowWithTwoColumns<A, B>> =
        ds.query(BuiltQuery.from(this)).unsafeCastToTwoColumns()

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<RowWithTwoColumns<A, B>> {
        override fun perform(ds: BlockingPerformer): RowSequence<RowWithTwoColumns<A, B>> =
            ds.query(BuiltQuery.from(this)).unsafeCastToTwoColumns()

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@QueryableOfTwo
        }
    }
}