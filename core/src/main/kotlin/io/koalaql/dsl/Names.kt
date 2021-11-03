package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.expr.Label

inline fun <reified T : Any> label(identifier: String? = null): Label<T> =
    Label(T::class, IdentifierName(identifier))