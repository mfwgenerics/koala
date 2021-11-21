package io.koalaql.identifier

sealed interface LabelIdentifier {
    companion object {
        operator fun invoke(name: String? = null) =
            name?.let { Named(name) } ?: Unnamed()
    }
}