package io.koalaql.window.built

import io.koalaql.window.FrameClauseType
import io.koalaql.window.FrameRangeMarker

class BuiltWindow {
    val partitions = BuiltWindowPartitions()

    var type: FrameClauseType? = null

    lateinit var from: FrameRangeMarker<*>
    var until: FrameRangeMarker<*>? = null
}