package io.koalaql.window.fluent

import io.koalaql.window.FrameRangeMarker
import io.koalaql.window.Window
import io.koalaql.window.built.BuildsIntoWindow
import io.koalaql.window.built.BuiltWindow

interface Betweenable<T>: BuildsIntoWindow {
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

    fun between(from: FrameRangeMarker<T>, until: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, until)
    fun start(from: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, null)
}