package io.koalaql.window

import io.koalaql.window.built.BuiltWindowPartitions
import io.koalaql.window.fluent.Partitionable

object GlobalWindow: Partitionable {
    override fun buildIntoWindowPartitions(partitions: BuiltWindowPartitions): Nothing? {
        return null
    }
}