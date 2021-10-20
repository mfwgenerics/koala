package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.Reference

interface ValuesWriter {
    operator fun <T : Any> set(reference: Reference<T>, value: Expr<T>)
    operator fun <T : Any> set(reference: Reference<T>, value: T?) =
        set(reference, Literal(reference.type, value))
}