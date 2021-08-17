package mfwgenerics.kotq.expr

import mfwgenerics.kotq.Distinctness

class DistinctAggregatable<T : Any>(
    val expr: Expr<T>
): OrderableAggregatable<T> {
    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.distinct = Distinctness.DISTINCT
        return expr
    }
}

fun <T : Any> distinct(expr: Expr<T>): OrderableAggregatable<T> =
    DistinctAggregatable(expr)