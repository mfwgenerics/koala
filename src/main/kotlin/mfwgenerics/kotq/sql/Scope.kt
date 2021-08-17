package mfwgenerics.kotq.sql

import mfwgenerics.kotq.Alias
import mfwgenerics.kotq.expr.AliasedName

/* TODO restrict names to identifier characters */
class Scope(
    val names: NameRegistry
) {
    private sealed interface Registered

    private data class UnderAlias(
        val alias: Alias,
        val innerName: AliasedName<*>
    ): Registered

    private object Internal: Registered

    private val registered = hashMapOf<AliasedName<*>, Registered>()
    private val aliases = hashMapOf<Alias, String>()

    private var aliasCount: Int = 0

    fun insert(
        name: AliasedName<*>,
        innerName: AliasedName<*>,
        alias: Alias
    ) {
        val value = UnderAlias(
            alias = alias,
            innerName = innerName
        )

        check(registered.put(name, value) == null)
            { "$name already in scope. value $value" }
    }

    fun insert(
        name: AliasedName<*>
    ) {
        check(registered.put(name, Internal) == null)
            { "$name already in scope" }
    }

    private fun generateAlias(): String = "T${aliasCount++}"

    fun register(alias: Alias, scope: Scope) {
        aliases[alias] = generateAlias()
    }

    operator fun get(name: AliasedName<*>): String {
        val registered = registered.getValue(name)

        return when (registered) {
            is UnderAlias -> "${aliases[registered.alias]}.${names[registered.innerName]}"
            Internal -> names[name]
        }
    }

    operator fun get(alias: Alias): String =
        aliases.getValue(alias)
}