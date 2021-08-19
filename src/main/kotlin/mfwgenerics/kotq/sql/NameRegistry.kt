package mfwgenerics.kotq.sql

import mfwgenerics.kotq.Alias
import mfwgenerics.kotq.expr.AliasedName
import mfwgenerics.kotq.window.WindowLabel

class NameRegistry {
    private val registered = hashMapOf<Any, String>()
    private var generated: Int = 0

    private fun generate(prefix: String): String = "$prefix${generated++}"

    operator fun get(name: AliasedName<*>): String =
        registered.getOrPut(name) { name
            .takeIf { it.aliases.isEmpty() }
            ?.identifier?.identifier?.asString
            ?: generate("n")
        }

    operator fun get(label: WindowLabel): String =
        registered.getOrPut(label) { label
            .identifier.asString
            ?: generate("w")
        }

    operator fun get(alias: Alias): String =
        registered.getOrPut(alias) { alias
            .identifier.asString
            ?: generate("T")
        }
}