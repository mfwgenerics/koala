package io.koalaql.dsl

import io.koalaql.expr.Label
import io.koalaql.identifier.LabelIdentifier
import kotlin.reflect.typeOf

inline fun <reified T : Any> label(identifier: String? = null): Label<T> =
    Label(typeOf<T>(), LabelIdentifier(identifier))