package mfwgenerics.kotq.window.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.window.FrameClauseType
import mfwgenerics.kotq.window.built.BuildsIntoWindow
import mfwgenerics.kotq.window.built.BuildsIntoWindowPartitions
import mfwgenerics.kotq.window.built.BuiltWindow

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