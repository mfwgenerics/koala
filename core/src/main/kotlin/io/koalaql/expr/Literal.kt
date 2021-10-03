package io.koalaql.expr

import kotlin.reflect.KClass

class Literal<T : Any>(
    val type: KClass<T>,
    val value: T?
): Expr<T> {
    override fun toString(): String = "${type.simpleName}($value)"
}