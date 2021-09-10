package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuiltSubquery

interface SubqueryExpr<T : Any>: Expr<T>, Subqueryable {
    class Wrap<T : Any>(
        private val subqueryable: Subqueryable
    ): SubqueryExpr<T> {
        override fun buildQuery(): BuiltSubquery =
            subqueryable.buildQuery()
    }
}