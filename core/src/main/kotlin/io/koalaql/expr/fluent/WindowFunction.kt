package io.koalaql.expr.fluent

import io.koalaql.expr.Expr
import io.koalaql.window.Window

interface WindowFunction<T : Any> {
    infix fun over(window: Window): Expr<T>
}