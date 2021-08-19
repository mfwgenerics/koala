package mfwgenerics.kotq.sql

import mfwgenerics.kotq.Alias
import mfwgenerics.kotq.expr.AliasedName
import mfwgenerics.kotq.window.WindowLabel

/* TODO restrict names to identifier characters */
class Scope(
    private val names: NameRegistry,
    private val enclosing: Scope? = null
) {
    fun innerScope(): Scope =
        Scope(names, this)

    private sealed interface Registered

    private class UnderAlias(
        val alias: Alias,
        val innerName: AliasedName<*>
    ): Registered

    private object Internal: Registered

    private class External(
        val symbol: String
    ): Registered

    class RegisteredAlias(
        val ident: String,
        val scope: Scope
    ) {
        override fun toString(): String = error("")
    }

    private val registered = hashMapOf<AliasedName<*>, Registered>()
    private val aliases = hashMapOf<Alias, RegisteredAlias>()

    fun insert(
        name: AliasedName<*>,
        innerName: AliasedName<*>,
        alias: Alias
    ) {
        val value = UnderAlias(
            alias = alias,
            innerName = innerName
        )

        registered.putIfAbsent(name, value)
    }

    fun insert(
        name: AliasedName<*>
    ) {
        registered.putIfAbsent(name, Internal)
    }

    fun external(
        name: AliasedName<*>,
        symbol: String
    ) {
        registered.putIfAbsent(name, External(symbol))
    }

    fun register(alias: Alias, scope: Scope) {
        aliases[alias] = RegisteredAlias(
            names[alias],
            scope
        )
    }

    operator fun get(name: AliasedName<*>): String {
        val registered = registered[name]
            ?: return enclosing?.get(name)!!

        return when (registered) {
            is UnderAlias -> {
                val alias = aliases.getValue(registered.alias)

                "${alias.ident}.${alias.scope[registered.innerName]}"
            }
            is External -> registered.symbol
            Internal -> names[name]
        }
    }

    operator fun get(alias: Alias): RegisteredAlias =
        aliases[alias]?:enclosing?.get(alias)!!

    fun nameOf(name: AliasedName<*>): String = names[name]
    fun nameOf(label: WindowLabel): String = names[label]

    fun allNames(): Collection<AliasedName<*>> = registered.keys
}