package io.koalaql.window.fluent

import io.koalaql.window.FrameRangeMarker
import io.koalaql.window.Window
import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.built.BuiltWindow

interface Betweenable<T>: WindowBuilder {
    private class RangedBetweenable<T>(
        val lhs: Betweenable<T>,
        val from: FrameRangeMarker<T>,
        val until: FrameRangeMarker<T>? = null
    ): Window {
        override fun BuiltWindow.buildIntoWindow(): WindowBuilder {
            from = this@RangedBetweenable.from
            until = this@RangedBetweenable.until
            return lhs
        }
    }

    fun between(from: FrameRangeMarker<T>, until: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, until)
    fun start(from: FrameRangeMarker<T>): Window =
        RangedBetweenable(this, from, null)
}