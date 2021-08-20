package mfwgenerics.kotq.window.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.window.built.BuiltWindowPartitions

interface Partitionable: WindowOrderable {
    private class PartitionedBy(
        val lhs: Partitionable,
        val partitions: List<Expr<*>>
    ): WindowOrderable {
        override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): Partitionable {
            partitions.partitions = this.partitions
            return lhs
        }
    }

    fun partitionBy(vararg exprs: Expr<*>): WindowOrderable =
        PartitionedBy(this, exprs.asList())
}