package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

abstract class NamedReference<T : Any>(
    override val type: KClass<T>,
    override val identifier: IdentifierName
): Reference<T> {
    override fun equals(other: Any?): Boolean =
        other is NamedReference<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}