package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.AliasedName

class NameRegistry {
    private val registered = hashMapOf<AliasedName<*>, String>()
    private var generated: Int = 0

    private fun generate(): String = "n${generated++}"

    operator fun get(name: AliasedName<*>): String =
        registered.getOrPut(name) { name
            .takeIf { it.aliases.isEmpty() }
            ?.name?.identifier?.asString
            ?: generate()
        }
}