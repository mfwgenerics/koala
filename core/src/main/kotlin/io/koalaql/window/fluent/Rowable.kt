package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.FrameClauseType
import io.koalaql.window.Window
import io.koalaql.window.built.BuildsIntoWindow
import io.koalaql.window.built.BuildsIntoWindowPartitions
import io.koalaql.window.built.BuiltWindow

interface Rowable: Window, BuildsIntoWindowPartitions {
    private class FrameClauseWindow<T>(
        val lhs: BuildsIntoWindow,
        val type: FrameClauseType
    ): Betweenable<T> {
        override fun buildIntoWindow(window: BuiltWindow): BuildsIntoWindow {
            window.type = type
            return lhs
        }
    }

    fun rows(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.ROWS)
    fun groups(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.GROUPS)
}