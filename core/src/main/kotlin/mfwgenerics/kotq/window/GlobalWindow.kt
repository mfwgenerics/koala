package mfwgenerics.kotq.window

import mfwgenerics.kotq.window.built.BuiltWindowPartitions
import mfwgenerics.kotq.window.fluent.Partitionable

object GlobalWindow: Partitionable {
    override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): Nothing? {
        return null
    }
}