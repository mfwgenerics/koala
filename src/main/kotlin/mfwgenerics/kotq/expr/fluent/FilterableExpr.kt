package mfwgenerics.kotq.expr.fluent

import mfwgenerics.kotq.expr.AggregatedExpr
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.built.BuiltAggregatedExpr

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