package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.expr.Name

inline fun <reified T : Any> name(identifier: String? = null): Name<T> =
    Name(T::class, IdentifierName(identifier))