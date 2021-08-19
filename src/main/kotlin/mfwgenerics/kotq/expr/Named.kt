package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

abstract class Named<T : Any>(
    override val type: KClass<T>,
    val identifier: IdentifierName
): Reference<T> {
    override fun buildIntoAliased(out: AliasedName<T>): Nothing? {
        out.identifier = identifier
        return null
    }

    override fun equals(other: Any?): Boolean =
        other is Name<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}