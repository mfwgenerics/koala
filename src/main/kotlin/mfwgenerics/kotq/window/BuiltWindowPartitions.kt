package mfwgenerics.kotq.window

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Ordinal

class BuiltWindowPartitions {
    var from: WindowLabel? = null

    var partitions: List<Expr<*>> = emptyList()
    var orderBy: List<Ordinal<*>> = emptyList()
}

class BuiltWindow {
    val partitions = BuiltWindowPartitions()

    var type: FrameClauseType? = null

    lateinit var from: FrameRangeMarker<*>
    var until: FrameRangeMarker<*>? = null
}