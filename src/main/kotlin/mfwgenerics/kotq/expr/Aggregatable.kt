package mfwgenerics.kotq.expr

import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoAggregatable {
    fun buildAggregatable(): BuiltAggregatable =
        unfoldBuilder(BuiltAggregatable()) { buildIntoAggregatable(it) }

    fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable?
}

interface Aggregatable<T : Any>: BuildsIntoAggregatable

interface OrderableAggregatable<T : Any>: Aggregatable<T> {
    fun orderBy(vararg ordinals: Ordinal<*>): Aggregatable<T> =
        OrderedAggregatable(this, ordinals.asList())
}

private class OrderedAggregatable<T : Any>(
    val lhs: OrderableAggregatable<T>,
    val ordinals: List<Ordinal<*>>
): Aggregatable<T> {
    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable {
        into.orderBy = ordinals
        return lhs
    }
}