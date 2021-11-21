package io.koalaql.dsl

import io.koalaql.expr.Label
import io.koalaql.identifier.LabelIdentifier

inline fun <reified T : Any> label(identifier: String? = null): Label<T> =
    Label(T::class, LabelIdentifier(identifier))