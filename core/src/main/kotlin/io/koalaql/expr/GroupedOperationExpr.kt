package io.koalaql.expr

import io.koalaql.expr.built.BuildsIntoAggregatedExpr
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.built.BuiltAggregatedExpr
import io.koalaql.expr.fluent.FilterableExpr

class GroupedOperationExpr<T : Any>(
    val type: OperationType,
    val args: Collection<BuiltAggregatable>
): FilterableExpr<T> {
    override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr? {
        aggregatedExpr.expr = this
        return null
    }
}