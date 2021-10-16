package io.koalaql.expr

import io.koalaql.expr.built.BuildsIntoAggregatedExpr
import io.koalaql.expr.built.BuiltAggregatedExpr
import io.koalaql.expr.fluent.WindowFunction
import io.koalaql.window.Window
import io.koalaql.window.built.BuiltWindow

class WindowFunctionExpr<T : Any>(
    private val expr: GroupedOperationExpr<T>
): WindowFunction<T> {
    private class OverWindow<T : Any>(
        val lhs: GroupedOperationExpr<T>,
        val window: Window
    ): AggregatedExpr<T> {
        override fun BuiltAggregatedExpr.buildIntoAggregatedExpr(): BuildsIntoAggregatedExpr? {
            over = BuiltWindow.from(window)
            expr = lhs
            return null
        }
    }

    override fun over(window: Window): Expr<T> = OverWindow(expr, window)
}