package io.koalaql.expr

import io.koalaql.query.Subqueryable
import io.koalaql.query.built.BuiltSubquery

interface SubqueryExpr<T : Any>: Expr<T>, Subqueryable {
    class Wrap<T : Any>(
        private val subqueryable: Subqueryable
    ): SubqueryExpr<T> {
        override fun buildQuery(): BuiltSubquery =
            subqueryable.buildQuery()
    }
}