package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.built.BuildsIntoWindow
import io.koalaql.window.built.BuiltWindow
import io.koalaql.window.built.BuiltWindowPartitions

interface Partitionable: WindowOrderable {
    private class PartitionedBy(
        val lhs: Partitionable,
        val partitions: List<Expr<*>>
    ): WindowOrderable {
        override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow? {
            window.partitions.partitions = this.partitions
            return lhs
        }
    }

    fun partitionBy(vararg exprs: Expr<*>): WindowOrderable =
        PartitionedBy(this, exprs.asList())
}