package mfwgenerics.kotq.sql

import mfwgenerics.kotq.Alias
import mfwgenerics.kotq.expr.Reference
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
        val innerName: Reference<*>
    ): Registered

    private object Internal: Registered

    class RegisteredAlias(
        val ident: String,
        val scope: Scope
    )

    private val external = hashMapOf<Reference<*>, String>()
    private val internal = hashMapOf<Reference<*>, Registered>()

    private val aliases = hashMapOf<Alias, RegisteredAlias>()

    fun external(name: Reference<*>, symbol: String? = null) {
        check (external.putIfAbsent(name, symbol?:names[name]) == null)
    }

    fun internal(
        name: Reference<*>,
        innerName: Reference<*>,
        alias: Alias
    ) {
        val value = UnderAlias(
            alias = alias,
            innerName = innerName
        )

        internal.putIfAbsent(name, value)
    }

    fun internal(name: Reference<*>) {
        check (internal.putIfAbsent(name, Internal) == null)
    }

    fun register(alias: Alias, scope: Scope) {
        aliases[alias] = RegisteredAlias(
            names[alias],
            scope
        )
    }

    operator fun get(name: Reference<*>): String {
        val registered = internal[name]
            ?: return checkNotNull(enclosing?.get(name)) {
                "$name not in scope ${System.identityHashCode(this)}"
            }

        return when (registered) {
            is UnderAlias -> {
                val alias = aliases.getValue(registered.alias)

                "${alias.ident}.${alias.scope.nameOf(registered.innerName)}"
            }
            Internal -> names[name]
        }
    }

    operator fun get(alias: Alias): RegisteredAlias =
        checkNotNull(aliases[alias]?:enclosing?.get(alias))
        { "alias $alias not found in scope ${System.identityHashCode(this)}" }

    fun nameOf(name: Reference<*>): String = external[name]!!
    fun nameOf(label: WindowLabel): String = names[label]

    fun externals(): Collection<Reference<*>> = external.keys
}