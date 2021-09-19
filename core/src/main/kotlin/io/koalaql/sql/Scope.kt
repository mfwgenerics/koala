package io.koalaql.sql

import io.koalaql.expr.Reference
import io.koalaql.query.Alias
import io.koalaql.query.Cte
import io.koalaql.query.LabelList
import io.koalaql.window.WindowLabel

/* TODO restrict names to identifier characters */
class Scope(
    val names: NameRegistry,
    private val enclosing: Scope? = null
) {
    fun innerScope(): Scope =
        Scope(names, this)

    private sealed interface Registered

    private class UnderAlias(
        val alias: Alias?,
        val innerName: String
    ): Registered

    private object Internal: Registered

    private val ctes = hashMapOf<Cte, LabelList>()
    private val external = hashMapOf<Reference<*>, String>()
    private val internal = hashMapOf<Reference<*>, Registered>()

    fun cte(cte: Cte, labels: LabelList) {
        ctes[cte] = labels
    }

    fun cteColumns(cte: Cte): LabelList = checkNotNull(ctes[cte]?:enclosing?.cteColumns(cte))
        { "missing cte $cte in scope $this" }

    fun external(name: Reference<*>, symbol: String? = null) {
        check (external.putIfAbsent(name, symbol?:names[name]) == null)
    }

    fun internal(
        name: Reference<*>,
        innerName: String,
        alias: Alias?
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

    fun resolveOrNull(name: Reference<*>): Resolved? {
        val registered = internal[name] ?: return enclosing?.resolveOrNull(name)

        return when (registered) {
            is UnderAlias -> Resolved(
                alias = registered.alias?.let { names[it] },
                innerName = registered.innerName
            )
            Internal -> Resolved(
                alias = null,
                innerName = names[name]
            )
        }
    }

    fun resolve(name: Reference<*>): Resolved = checkNotNull(resolveOrNull(name))
        { "$name not in $this" }

    operator fun get(alias: Alias): String = names[alias]
    operator fun get(cte: Cte): String = names[cte]

    fun nameOf(name: Reference<*>): String = external[name]?:names[name]
    fun nameOf(label: WindowLabel): String = names[label]

    override fun toString(): String = "scope-${System.identityHashCode(this)}"
}