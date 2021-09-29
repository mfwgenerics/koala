package io.koalaql.expr.fluent

import io.koalaql.expr.AggregatedExpr
import io.koalaql.expr.built.BuiltAggregatedExpr
import io.koalaql.window.Window

interface OverableAggregateExpr<T : Any>: AggregatedExpr<T> {
    private class OverWindow<T : Any>(
        val lhs: OverableAggregateExpr<T>,
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