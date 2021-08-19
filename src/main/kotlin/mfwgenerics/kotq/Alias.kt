package mfwgenerics.kotq

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.Reference

class Alias(
    val identifier: IdentifierName = IdentifierName()
) {
    operator fun <T : Any> get(reference: Reference<T>) = AliasedReference(reference.type, this, reference)

    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}