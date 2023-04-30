package io.koalaql.expr

import io.koalaql.identifier.LabelIdentifier
import io.koalaql.query.Alias
import kotlin.reflect.KType

sealed interface Reference<T : Any>: SelectOperand<T> {
    val type: KType

    val identifier: LabelIdentifier?

    override fun SelectionBuilder.buildIntoSelection() {
        expression(this@Reference, this@Reference)
    }

    override fun asReference(): Reference<T> = this

    /* matches x from Excluded[x]. for supporting EXCLUDED.x or VALUES(x) in ON CONFLICT/ON DUPLICATE UPDATE*/
    fun excludedReference(): Reference<T>? = null
}

class AliasedReference<T : Any>(
    override val type: KType,
    private val alias: Alias,
    private val reference: Reference<T>
): Reference<T> {
    override val identifier: LabelIdentifier? get() = null

    override fun excludedReference(): Reference<T>? = reference.takeIf { alias === EXCLUDED_MARKER_ALIAS }

    override fun equals(other: Any?): Boolean =
        other is AliasedReference<*> &&
        alias.identifier == other.alias.identifier &&
        reference == other.reference

    override fun hashCode(): Int = alias.identifier.hashCode() xor reference.hashCode()
    override fun toString(): String = "${alias.identifier}.${reference}"
}

abstract class NamedReference<T : Any>(
    override val type: KType,
    override val identifier: LabelIdentifier
): Reference<T> {
    override fun equals(other: Any?): Boolean =
        other is NamedReference<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}
