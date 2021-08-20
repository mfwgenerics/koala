package mfwgenerics.kotq.expr

import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.Distinctness

class DistinctAggregatable<T : Any>(
    val expr: Expr<T>
): OrderableAggregatable<T> {
    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.distinct = Distinctness.DISTINCT
        return expr
    }
}