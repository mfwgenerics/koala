package io.koalaql.query

import io.koalaql.expr.AliasedReference
import io.koalaql.expr.AsReference
import io.koalaql.identifier.LabelIdentifier
import io.koalaql.identifier.Unnamed

class Alias(
    val identifier: LabelIdentifier = Unnamed()
): GetsAliasedReference {
    constructor(identifier: String): this(LabelIdentifier(identifier))

    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> {
        val actual = reference.asReference()

        return AliasedReference(actual.type, this, actual)
    }

    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}