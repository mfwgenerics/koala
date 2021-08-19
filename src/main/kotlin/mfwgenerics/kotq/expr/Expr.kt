package mfwgenerics.kotq.expr

import mfwgenerics.kotq.Alias
import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.unfoldBuilder
import kotlin.reflect.KClass

sealed interface Expr<T : Any>: Ordinal<T>, OrderableAggregatable<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)

    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.expr = this
        return null
    }
}

sealed interface Reference<T : Any>: Expr<T>, NamedExprs {
    val type: KClass<T>

    val identifier: IdentifierName?

    override fun namedExprs(): List<Labeled<*>> =
        listOf(Labeled(this, this))
}

class AliasedReference<T : Any>(
    override val type: KClass<T>,
    val of: Alias,
    val reference: Reference<T>
): Reference<T>, NamedExprs {
    override val identifier: IdentifierName? get() = null

    override fun equals(other: Any?): Boolean =
        other is AliasedReference<*> &&
        of.identifier == other.of.identifier &&
        reference == other.reference

    override fun hashCode(): Int = of.identifier.hashCode() xor reference.hashCode()
    override fun toString(): String = "${of.identifier}.${reference}"
}