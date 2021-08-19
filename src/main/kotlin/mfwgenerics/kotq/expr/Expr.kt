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

class AliasedName<T : Any>(
    val aliases: MutableList<Alias> = arrayListOf(),
) {
    lateinit var identifier: IdentifierName

    fun copyWithPrefix(alias: Alias) = AliasedName<T>(
        arrayListOf(alias).apply { addAll(aliases) }
    ).also { it.identifier = identifier }

    override fun hashCode(): Int {
        var result = identifier.hashCode()

        aliases.forEach { result = result xor it.hashCode() }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AliasedName<*>) return false
        if (aliases.size != other.aliases.size) return false

        repeat(aliases.size) {
            if (aliases[it] != other.aliases[it]) return false
        }

        return identifier == other.identifier
    }

    override fun toString(): String = if (aliases.isNotEmpty()) {
        "${aliases.joinToString(".")}.$identifier"
    } else {
        "$identifier"
    }
}

sealed interface Reference<T : Any>: Expr<T>, NamedExprs {
    val type: KClass<T>

    override fun namedExprs(): List<Labeled<*>> =
        listOf(Labeled(this, buildAliased()))

    fun buildAliased(): AliasedName<T> =
        unfoldBuilder(AliasedName<T>()) { buildIntoAliased(it) }

    fun buildIntoAliased(out: AliasedName<T>): Reference<T>?
}

class AliasedReference<T : Any>(
    override val type: KClass<T>,
    val of: Alias,
    val reference: Reference<T>
): Reference<T>, NamedExprs {
    override fun buildIntoAliased(out: AliasedName<T>): Reference<T>? {
        out.aliases.add(of)
        return reference
    }

    override fun equals(other: Any?): Boolean =
        other is AliasedReference<*> &&
        of.identifier == other.of.identifier &&
        reference == other.reference

    override fun hashCode(): Int = of.identifier.hashCode() xor reference.hashCode()
    override fun toString(): String = "${of.identifier}.${reference}"
}