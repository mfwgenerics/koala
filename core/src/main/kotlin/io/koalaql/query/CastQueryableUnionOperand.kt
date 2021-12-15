package io.koalaql.query

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith
import io.koalaql.query.built.QueryBuilder
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

open class CastQueryableUnionOperand<T>(
    val of: QueryableUnionOperand<*>,
    val cast: (rows: RowSequence<RawResultRow>) -> RowSequence<T>
): QueryableUnionOperand<T> {
    override fun perform(ds: BlockingPerformer): RowSequence<T> =
        cast(ds.query(with (this) { BuilderContext.buildQuery() }))

    override fun BuiltQuery.buildInto(): QueryBuilder = of

    override fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness) {
        with (of) { buildIntoQueryTail(type, distinctness) }
    }

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<T> {
        override fun perform(ds: BlockingPerformer): RowSequence<T> =
            cast(ds.query(with (this) { BuilderContext.buildQuery() }))

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@CastQueryableUnionOperand
        }
    }
}