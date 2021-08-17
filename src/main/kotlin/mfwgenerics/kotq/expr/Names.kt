package mfwgenerics.kotq.expr

inline fun <reified T : Any> name(identifier: String? = null): Name<T> =
    Name(T::class, identifier)