package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.FrameClauseType
import io.koalaql.window.built.BuildsIntoWindow
import io.koalaql.window.built.BuildsIntoWindowPartitions
import io.koalaql.window.built.BuiltWindow

interface Rangeable: Rowable, BuildsIntoWindowPartitions {
    private class RangeClauseWindow<T>(
        val lhs: BuildsIntoWindow
    ): Betweenable<T> {
        override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow {
            window.type = FrameClauseType.RANGE
            return lhs
        }
    }

    fun range(): Betweenable<Expr<*>> =
        RangeClauseWindow(this)
}