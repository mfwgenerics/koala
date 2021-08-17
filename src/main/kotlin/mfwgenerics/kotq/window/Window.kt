package mfwgenerics.kotq.window

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoWindow {
    fun buildWindow(): BuiltWindow =
        unfoldBuilder(BuiltWindow()) { buildIntoWindow(it) }

    fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow?
}

interface BuildsIntoWindowPartitions: BuildsIntoWindow {
    fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): BuildsIntoWindowPartitions?

    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow? =
        buildIntoWindowPartitions(window.partitions)
}

interface Window: BuildsIntoWindow {
}

interface Betweenable<T>: BuildsIntoWindow {
    fun between(from: FrameRangeMarker<T>, until: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, until)
    fun start(from: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, null)
}

private class RangedBetweenable<T>(
    val lhs: Betweenable<T>,
    val from: FrameRangeMarker<T>,
    val until: FrameRangeMarker<T>? = null
): Window {
    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow {
        window.from = from
        window.until = until
        return lhs
    }
}

interface Rowable: Window, BuildsIntoWindowPartitions {
    fun rows(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.ROWS)
    fun groups(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.GROUPS)
}

interface Rangeable: Rowable, BuildsIntoWindowPartitions {
    fun range(): Betweenable<Expr<*>> =
        FrameClauseWindow(this, FrameClauseType.RANGE)
}

private class FrameClauseWindow<T>(
    val lhs: BuildsIntoWindow,
    val type: FrameClauseType
): Betweenable<T> {
    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow {
        window.type = type
        return lhs
    }
}

interface OrderableWindow: Rowable {
    fun orderBy(ordinal: Ordinal<*>): Rangeable =
        OrderBy(this, listOf(ordinal))
    fun orderBy(first: Ordinal<*>, second: Ordinal<*>, vararg ordinals: Ordinal<*>): Rowable =
        OrderBy(this, listOf(first, second, *ordinals))
}

private class OrderBy(
    val lhs: OrderableWindow,
    val orderBy: List<Ordinal<*>>
): Rangeable {
    override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): OrderableWindow? {
        partitions.orderBy = orderBy
        return lhs
    }
}

interface Partitionable: OrderableWindow {
    fun partitionBy(vararg exprs: Expr<*>): OrderableWindow =
        PartitionedBy(this, exprs.asList())
}

private class PartitionedBy(
    val lhs: Partitionable,
    val partitions: List<Expr<*>>
): OrderableWindow {
    override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): Partitionable {
        partitions.partitions = this.partitions
        return lhs
    }
}

object GlobalWindow: Partitionable {
    override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): Nothing? {
        return null
    }
}