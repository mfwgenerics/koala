package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface SubqueryExpr<T : Any>: Expr<T>, Queryable<ResultRow> {
    class Wrap<T : Any>(
        private val queryable: Queryable<ResultRow>
    ): SubqueryExpr<T> {
        override fun buildQuery(): BuiltSubquery =
            queryable.buildQuery()
    }

    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> = ds.query(buildQuery())
}