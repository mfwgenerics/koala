package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.built.BuiltWindow
import io.koalaql.window.built.WindowBuilder

interface Partitionable: WindowOrderable {
    private class PartitionedBy(
        val lhs: Partitionable,
        val partitions: List<Expr<*>>
    ): WindowOrderable {
        override fun BuiltWindow.buildIntoWindow(): WindowBuilder? {
            partitions.partitions = this@PartitionedBy.partitions
            return lhs
        }
    }

    fun partitionBy(vararg exprs: Expr<*>): WindowOrderable =
        PartitionedBy(this, exprs.asList())
}