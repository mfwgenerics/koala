package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.WithType
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith
import io.koalaql.query.built.QueryBuilder
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RowOfOne
import io.koalaql.values.RowSequence
import io.koalaql.values.unsafeCastToOneColumn

interface ExprQueryableUnionOperand<T : Any>: ExprQueryable<T>, QueryableUnionOperand<RowOfOne<T>> {
    override fun perform(ds: BlockingPerformer): RowSequence<RowOfOne<T>> =
        ds.query(with (this) { BuilderContext.buildQuery() }).unsafeCastToOneColumn()

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<RowOfOne<T>> {
        override fun perform(ds: BlockingPerformer): RowSequence<RowOfOne<T>> =
            ds.query(with (this) { BuilderContext.buildQuery() }).unsafeCastToOneColumn()

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@ExprQueryableUnionOperand
        }
    }
}