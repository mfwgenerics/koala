package mfwgenerics.kotq.window.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.window.FrameClauseType
import mfwgenerics.kotq.window.Window
import mfwgenerics.kotq.window.built.BuildsIntoWindow
import mfwgenerics.kotq.window.built.BuildsIntoWindowPartitions
import mfwgenerics.kotq.window.built.BuiltWindow

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