package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.AliasedName
import mfwgenerics.kotq.expr.Name

/* TODO restrict names to identifier characters */
class Scope {
    private val registered = hashMapOf<AliasedName<*>, String>()
    private var anonymous: Int = 0

    fun insert(name: Name<*>, value: String) {
        check(registered.put(name.toAliasedName(), value) == null)
    }

    private fun generate(): String =
        "nm${anonymous++}"

    operator fun get(name: AliasedName<*>): String =
        registered.getOrPut(name) { generate() }
}