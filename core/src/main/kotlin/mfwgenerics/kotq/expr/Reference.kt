package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.query.Alias
import kotlin.reflect.KClass

sealed interface Reference<T : Any>: AsReference<T>, SelectArgument {
    val type: KClass<T>

    val identifier: IdentifierName?

    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.expression(this, this)
    }

    override fun asReference(): Reference<T> = this
}

class AliasedReference<T : Any>(
    override val type: KClass<T>,
    private val of: Alias,
    private val reference: Reference<T>
): Reference<T>, SelectArgument {
    override val identifier: IdentifierName? get() = null

    override fun equals(other: Any?): Boolean =
        other is AliasedReference<*> &&
        of.identifier == other.of.identifier &&
        reference == other.reference

    override fun hashCode(): Int = of.identifier.hashCode() xor reference.hashCode()
    override fun toString(): String = "${of.identifier}.${reference}"
}

abstract class NamedReference<T : Any>(
    override val type: KClass<T>,
    override val identifier: IdentifierName
): Reference<T> {
    override fun equals(other: Any?): Boolean =
        other is NamedReference<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}
