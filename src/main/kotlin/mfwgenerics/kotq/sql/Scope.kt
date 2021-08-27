package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.window.WindowLabel

/* TODO restrict names to identifier characters */
class Scope(
    val names: NameRegistry,
    private val enclosing: Scope? = null
) {
    fun innerScope(): Scope =
        Scope(names, this)

    private sealed interface Registered

    private class UnderAlias(
        val alias: Alias,
        val innerName: String
    ): Registered

    private object Internal: Registered

    private val external = hashMapOf<Reference<*>, String>()
    private val internal = hashMapOf<Reference<*>, Registered>()

    fun external(name: Reference<*>, symbol: String? = null) {
        check (external.putIfAbsent(name, symbol?:names[name]) == null)
    }

    fun internal(
        name: Reference<*>,
        innerName: String,
        alias: Alias
    ) {
        val value = UnderAlias(
            alias = alias,
            innerName = innerName
        )

        internal.putIfAbsent(name, value)
    }

    fun internal(name: Reference<*>) {
        internal.putIfAbsent(name, Internal)
    }

    operator fun get(name: Reference<*>): String {
        val registered = internal[name]
            ?: return checkNotNull(enclosing?.get(name)) {
                "$name not in scope ${System.identityHashCode(this)}"
            }

        return when (registered) {
            is UnderAlias -> {
                "${names[registered.alias]}.${registered.innerName}"
            }
            Internal -> names[name]
        }
    }

    operator fun get(alias: Alias): String = names[alias]
    operator fun get(cte: Cte): String = names[cte]

    fun nameOf(name: Reference<*>): String = external[name]!!
    fun nameOf(label: WindowLabel): String = names[label]

    override fun toString(): String = "scope-${System.identityHashCode(this)}"
}