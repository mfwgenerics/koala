package mfwgenerics.kotq.expr

import mfwgenerics.kotq.Alias

sealed interface Expr<T : Any>: Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)
}

data class AliasedName<T : Any>(
    val aliases: MutableList<Alias> = arrayListOf(),
) {
    lateinit var name: Name<T>
}

sealed interface Reference<T : Any>: Expr<T>, NameGroup {
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
): Reference<T> {
    override fun buildAliasedName(out: AliasedName<T>): Reference<T> {
        out.aliases.add(of)
        return reference
    }
}