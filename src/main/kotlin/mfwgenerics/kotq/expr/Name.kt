package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

class Name<T : Any>(
    val type: KClass<T>,
    val identifier: IdentifierName
): Named<T> {
    override val name: Name<T> get() = this

    override fun equals(other: Any?): Boolean =
        other is Name<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}