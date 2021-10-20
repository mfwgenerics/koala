package io.koalaql.window

import io.koalaql.window.built.BuiltWindow
import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.fluent.Partitionable

object GlobalWindow: Partitionable {
    override fun BuiltWindow.buildIntoWindow(): WindowBuilder? {
        return null
    }
}