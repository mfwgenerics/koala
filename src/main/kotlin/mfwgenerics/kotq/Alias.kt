package mfwgenerics.kotq

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.Reference

class Alias(
    private val name: String? = null
) {
    operator fun <T : Any> get(reference: Reference<T>) = AliasedReference(this, reference)

    override fun toString(): String = "$name"
}