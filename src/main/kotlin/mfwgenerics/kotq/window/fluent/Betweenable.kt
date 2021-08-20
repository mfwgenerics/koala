package mfwgenerics.kotq.window.fluent

import mfwgenerics.kotq.window.FrameRangeMarker
import mfwgenerics.kotq.window.Window
import mfwgenerics.kotq.window.built.BuildsIntoWindow
import mfwgenerics.kotq.window.built.BuiltWindow

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