package mfwgenerics.kotq.query

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.AsReference
import mfwgenerics.kotq.expr.Reference

class Alias(
    val identifier: IdentifierName = IdentifierName()
): GetsAliasedReference {
    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> {
        val actual = reference.asReference()

        return AliasedReference(actual.type, this, actual)
    }

    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}