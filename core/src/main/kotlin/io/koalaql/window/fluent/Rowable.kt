package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.FrameClauseType
import io.koalaql.window.Window
import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.built.BuiltWindow

interface Rowable: Window, WindowBuilder {
    private class FrameClauseWindow<T>(
        val lhs: WindowBuilder,
        val type: FrameClauseType
    ): Betweenable<T> {
        override fun BuiltWindow.buildIntoWindow(): WindowBuilder {
            type = this@FrameClauseWindow.type
            return lhs
        }
    }

    fun rows(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.ROWS)
    fun groups(): Betweenable<Expr<Int>> =
        FrameClauseWindow(this, FrameClauseType.GROUPS)
}