package io.koalaql.expr

import io.koalaql.expr.built.BuiltAggregatable

interface AggregatableBuilder {
    fun BuiltAggregatable.buildIntoAggregatable(): AggregatableBuilder?
}

interface Aggregatable<T : Any>: AggregatableBuilder

interface OrderableAggregatable<T : Any>: Aggregatable<T> {
    private class OrderedAggregatable<T : Any>(
        val lhs: OrderableAggregatable<T>,
        val ordinals: List<Ordinal<*>>
    ): Aggregatable<T> {
        override fun BuiltAggregatable.buildIntoAggregatable(): AggregatableBuilder {
            orderBy = ordinals
            return lhs
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Aggregatable<T> =
        OrderedAggregatable(this, ordinals.asList())
}