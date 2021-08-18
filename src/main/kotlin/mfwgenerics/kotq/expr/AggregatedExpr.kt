package mfwgenerics.kotq.expr

import mfwgenerics.kotq.unfoldBuilder
import mfwgenerics.kotq.window.Window

interface BuildsIntoAggregatedExpr {
    fun buildAggregated(): BuiltAggregatedExpr =
        unfoldBuilder(BuiltAggregatedExpr()) { buildIntoGroupExpr(it) }

    fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr?
}

interface AggregatedExpr<T : Any>: Expr<T>, BuildsIntoAggregatedExpr

interface OverableExpr<T : Any>: AggregatedExpr<T> {
    infix fun over(window: Window): AggregatedExpr<T> =
        OverWindow(this, window)
}

private class OverWindow<T : Any>(
    val lhs: OverableExpr<T>,
    val window: Window
): AggregatedExpr<T> {
    override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): AggregatedExpr<*>? {
        aggregatedExpr.over = window.buildWindow()
        return lhs
    }
}

interface FilterableExpr<T : Any>: OverableExpr<T> {
    fun filter(filter: Expr<Boolean>): OverableExpr<T> =
        Filtered(this, filter)
}

private class Filtered<T : Any>(
    val lhs: FilterableExpr<T>,
    val filter: Expr<Boolean>
): OverableExpr<T> {
    override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): AggregatedExpr<*>? {
        aggregatedExpr.filter = filter
        return lhs
    }
}

class GroupedOperationExpr<T : Any>(
    val type: GroupedOperationType,
    val args: Collection<BuiltAggregatable>
): FilterableExpr<T> {
    override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr? {
        aggregatedExpr.expr = this
        return null
    }
}