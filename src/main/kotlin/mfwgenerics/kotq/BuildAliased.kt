package mfwgenerics.kotq

import mfwgenerics.kotq.expr.AliasedName

interface BuildAliased<T : Any> {
    fun buildAliased(): AliasedName<T> {
        val result = AliasedName<T>()

        var next = buildIntoAliased(result)

        while (next != null) next = next.buildIntoAliased(result)

        return result
    }

    fun buildIntoAliased(out: AliasedName<T>): BuildAliased<T>?
}