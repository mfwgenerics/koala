package io.koalaql.window.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.FrameClauseType
import io.koalaql.window.built.WindowBuilder
import io.koalaql.window.built.BuiltWindow

interface Rangeable: Rowable, WindowBuilder {
    private class RangeClauseWindow<T>(
        val lhs: WindowBuilder
    ): Betweenable<T> {
        override fun BuiltWindow.buildIntoWindow(): WindowBuilder {
            type = FrameClauseType.RANGE
            return lhs
        }
    }

    fun range(): Betweenable<Expr<*>> =
        RangeClauseWindow(this)
}