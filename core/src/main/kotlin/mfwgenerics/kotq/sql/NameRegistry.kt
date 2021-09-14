package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.window.WindowLabel

class NameRegistry {
    private val registered = hashMapOf<Any, String>()
    private var generated: Int = 0

    private fun generate(prefix: String): String = "$prefix${generated++}"

    operator fun get(cte: Cte): String =
        registered.getOrPut(cte.identifier) { cte
            .identifier.asString
            ?: generate("T")
        }

    operator fun get(name: Reference<*>): String =
        registered.getOrPut(name) { name
            .identifier?.asString
            ?: generate("n")
        }

    operator fun get(label: WindowLabel): String =
        registered.getOrPut(label) { label
            .identifier.asString
            ?: generate("w")
        }

    operator fun get(alias: Alias): String =
        registered.getOrPut(alias.identifier) { alias
            .identifier.asString
            ?: generate("T")
        }
}