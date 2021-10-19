package io.koalaql.expr.fluent

import io.koalaql.expr.Expr

interface ThenableCase<S : Any, T : Any> {
    infix fun <R : T> then(expr: Expr<R>): WhenableCase<S, R>
}