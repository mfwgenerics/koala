package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithOneColumn
import io.koalaql.values.unsafeCastToOneColumn

interface QueryableOfOne<T : Any>: Expr<T>, Queryable<RowWithOneColumn<T>> {
    class Wrap<T : Any>(
        private val queryable: Queryable<*>
    ): QueryableOfOne<T> {
        override fun buildQuery(): BuiltSubquery =
            queryable.buildQuery()
    }

    override fun performWith(ds: BlockingPerformer): RowSequence<RowWithOneColumn<T>> =
        ds.query(buildQuery()).unsafeCastToOneColumn()
}