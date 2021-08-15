package mfwgenerics.kotq

class Alias {
    operator fun <T : Any> get(reference: Reference<T>) = AliasedReference(this, reference)
}