package mfwgenerics.kotq.window.built

import mfwgenerics.kotq.window.FrameClauseType
import mfwgenerics.kotq.window.FrameRangeMarker

class BuiltWindow {
    val partitions = BuiltWindowPartitions()

    var type: FrameClauseType? = null

    lateinit var from: FrameRangeMarker<*>
    var until: FrameRangeMarker<*>? = null
}