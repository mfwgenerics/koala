package io.koalaql.window

import io.koalaql.window.built.BuildsIntoWindow
import io.koalaql.window.built.BuiltWindow
import io.koalaql.window.built.BuiltWindowPartitions
import io.koalaql.window.fluent.Partitionable

object GlobalWindow: Partitionable {
    override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow? {
        return null
    }
}