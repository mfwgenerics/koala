package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.WithType
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith
import io.koalaql.query.built.QueryBuilder
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface UnionedQueryable: WithableQueryable<ResultRow> {
    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<ResultRow> {
        override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(BuiltQuery.from(this))

        override fun BuiltQuery.buildInto(): QueryBuilder? {
            withType = type
            withs = queries

            return this@UnionedQueryable
        }
    }

    override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
        ds.query(BuiltQuery.from(this))
}