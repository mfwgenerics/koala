package io.koalaql.expr

import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltSubquery

interface SubqueryExpr<T : Any>: Expr<T>, Queryable {
    class Wrap<T : Any>(
        private val queryable: Queryable
    ): SubqueryExpr<T> {
        override fun buildQuery(): BuiltSubquery =
            queryable.buildQuery()
    }
}