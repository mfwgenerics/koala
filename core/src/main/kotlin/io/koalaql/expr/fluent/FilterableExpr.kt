package io.koalaql.expr.fluent

import io.koalaql.expr.AggregatedExpr
import io.koalaql.expr.Expr
import io.koalaql.expr.built.BuiltAggregatedExpr

interface FilterableExpr<T : Any>: OverableAggregateExpr<T> {
    private class Filtered<T : Any>(
        val lhs: FilterableExpr<T>,
        val filter: Expr<Boolean>
    ): OverableAggregateExpr<T> {
        override fun BuiltAggregatedExpr.buildIntoAggregatedExpr(): AggregatedExpr<*>? {
            filter = this@Filtered.filter
            return lhs
        }
    }

    fun filter(filter: Expr<Boolean>): OverableAggregateExpr<T> =
        Filtered(this, filter)
}