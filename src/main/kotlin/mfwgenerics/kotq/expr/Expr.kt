package mfwgenerics.kotq.expr

import mfwgenerics.kotq.Alias

sealed interface Expr<T : Any>: Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)
}

class AliasedName<T : Any>(
    val aliases: MutableList<Alias> = arrayListOf(),
) {
    lateinit var name: Name<T>

    override fun hashCode(): Int {
        var result = name.hashCode()

        aliases.forEach { result = result xor it.hashCode() }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AliasedName<*>) return false
        if (aliases.size != other.aliases.size) return false

        repeat(aliases.size) {
            if (aliases[it] != other.aliases[it]) return false
        }

        return name == other.name
    }

    override fun toString(): String = if (aliases.isNotEmpty()) {
        "${aliases.joinToString(".")}.$name"
    } else {
        "$name"
    }
}

sealed interface Reference<T : Any>: Expr<T>, NamedExprs {
    fun toAliasedName(): AliasedName<T> {
        val result = AliasedName<T>()

        var next = buildAliasedName(result)

        while (next != null) next = next.buildAliasedName(result)

        return result
    }

    fun buildAliasedName(out: AliasedName<T>): Reference<T>?
}

class AliasedReference<T : Any>(
    val of: Alias,
    val reference: Reference<T>
): Reference<T>, NamedExprs {
    override fun namedExprs(): List<SelectedExpr<*>> = listOf(
        SelectedExpr(
            expr = reference,
            name = toAliasedName()
        )
    )

    override fun buildAliasedName(out: AliasedName<T>): Reference<T> {
        out.aliases.add(of)
        return reference
    }
}