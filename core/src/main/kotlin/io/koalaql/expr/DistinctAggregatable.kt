package io.koalaql.expr

import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.Distinctness

class DistinctAggregatable<T : Any>(
    val expr: Expr<T>
): OrderableAggregatable<T> {
    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.distinct = Distinctness.DISTINCT
        return expr
    }
}