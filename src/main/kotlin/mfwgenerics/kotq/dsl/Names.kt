package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.Name

inline fun <reified T : Any> name(identifier: String? = null): Name<T> =
    Name(T::class, IdentifierName(identifier))