package mfwgenerics.kotq

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.Reference

class Alias(
    private val name: String? = null
) {
    operator fun <T : Any> get(reference: Reference<T>) = AliasedReference(this, reference)

    override fun equals(other: Any?): Boolean {
        if (other !is Alias) return false

        if (name == null && other.name == null) return this === other

        return name == other.name
    }

    override fun hashCode(): Int =
        name?.hashCode()?:System.identityHashCode(this)

    override fun toString(): String = if (name != null) "$name" else "alias"
}