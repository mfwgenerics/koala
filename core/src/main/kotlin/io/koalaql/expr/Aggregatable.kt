package io.koalaql.expr

import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.unfoldBuilder

interface BuildsIntoAggregatable {
    fun buildAggregatable(): BuiltAggregatable =
        unfoldBuilder(BuiltAggregatable()) { buildIntoAggregatable(it) }

    fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable?
}

interface Aggregatable<T : Any>: BuildsIntoAggregatable

interface OrderableAggregatable<T : Any>: Aggregatable<T> {
    private class OrderedAggregatable<T : Any>(
        val lhs: OrderableAggregatable<T>,
        val ordinals: List<Ordinal<*>>
    ): Aggregatable<T> {
        override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable {
            into.orderBy = ordinals
            return lhs
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Aggregatable<T> =
        OrderedAggregatable(this, ordinals.asList())
}