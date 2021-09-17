package io.koalaql.window.fluent

import io.koalaql.expr.Ordinal
import io.koalaql.window.built.BuiltWindowPartitions

interface WindowOrderable: Rowable {
    private class OrderBy(
        val lhs: WindowOrderable,
        val orderBy: List<Ordinal<*>>
    ): Rangeable {
        override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): WindowOrderable? {
            partitions.orderBy = orderBy
            return lhs
        }
    }

    fun orderBy(ordinal: Ordinal<*>): Rangeable =
        OrderBy(this, listOf(ordinal))
    fun orderBy(first: Ordinal<*>, second: Ordinal<*>, vararg ordinals: Ordinal<*>): Rowable =
        OrderBy(this, listOf(first, second, *ordinals))
}