package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName

inline fun <reified T : Any> name(identifier: String? = null): Name<T> =
    Name(T::class, IdentifierName(identifier))