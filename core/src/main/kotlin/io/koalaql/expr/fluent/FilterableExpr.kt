package io.koalaql.expr.fluent

import io.koalaql.expr.AggregatedExpr
import io.koalaql.expr.Expr
import io.koalaql.expr.built.BuiltAggregatedExpr

interface FilterableExpr<T : Any>: OverableExpr<T> {
    private class Filtered<T : Any>(
        val lhs: FilterableExpr<T>,
        val filter: Expr<Boolean>
    ): OverableExpr<T> {
        override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): AggregatedExpr<*>? {
            aggregatedExpr.filter = filter
            return lhs
        }
    }

    fun filter(filter: Expr<Boolean>): OverableExpr<T> =
        Filtered(this, filter)
}