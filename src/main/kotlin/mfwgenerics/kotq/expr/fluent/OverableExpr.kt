package mfwgenerics.kotq.expr.fluent

import mfwgenerics.kotq.expr.AggregatedExpr
import mfwgenerics.kotq.expr.built.BuiltAggregatedExpr
import mfwgenerics.kotq.window.Window

interface OverableExpr<T : Any>: AggregatedExpr<T> {
    private class OverWindow<T : Any>(
        val lhs: OverableExpr<T>,
        val window: Window
    ): AggregatedExpr<T> {
        override fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): AggregatedExpr<*>? {
            aggregatedExpr.over = window.buildWindow()
            return lhs
        }
    }

    infix fun over(window: Window): AggregatedExpr<T> =
        OverWindow(this, window)
}