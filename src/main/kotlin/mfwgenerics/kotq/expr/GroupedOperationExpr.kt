package mfwgenerics.kotq.expr

import mfwgenerics.kotq.expr.built.BuildsIntoAggregatedExpr
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.expr.built.BuiltAggregatedExpr
import mfwgenerics.kotq.expr.fluent.FilterableExpr

class GroupedOperationExpr<T : Any>(
    val type: GroupedOperationType,
    val args: Collection<BuiltAggregatable>
): FilterableExpr<T> {
    override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr? {
        aggregatedExpr.expr = this
        return null
    }
}