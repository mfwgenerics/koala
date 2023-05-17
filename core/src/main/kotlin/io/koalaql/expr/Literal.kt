package io.koalaql.expr

import kotlin.reflect.KType

class Literal<T : Any>(
    val type: KType,
    val value: T?
): Expr<T> {
    override fun toString(): String = "($value): $type"
}