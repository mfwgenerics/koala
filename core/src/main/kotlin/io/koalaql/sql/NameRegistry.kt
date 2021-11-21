package io.koalaql.sql

import io.koalaql.expr.Reference
import io.koalaql.identifier.LabelIdentifier
import io.koalaql.identifier.Named
import io.koalaql.identifier.Unnamed
import io.koalaql.query.Alias
import io.koalaql.query.Cte
import io.koalaql.window.WindowLabel

class NameRegistry(
    private val positionalName: (Int) -> String? = { null }
) {
    private val registered = hashMapOf<Any, String>()

    private var tableN: Int = 0
    private var columnsN: Int = 0
    private var windowsN: Int = 0

    private fun generateT(): String = "T${tableN++}"
    private fun generatec(): String = "c${columnsN++}"
    private fun generatew(): String = "w${windowsN++}"

    private val LabelIdentifier.asString: String? get() = when (this) {
        is Unnamed -> null
        is Named -> name
    }

    operator fun get(cte: Cte): String =
        registered.getOrPut(cte.identifier) { cte
            .identifier.asString
            ?: generateT()
        }

    operator fun get(name: Reference<*>): String =
        registered.getOrPut(name) { name
            .identifier?.asString
            ?: generatec()
        }

    operator fun get(label: WindowLabel): String =
        registered.getOrPut(label) { label
            .identifier.asString
            ?: generatew()
        }

    operator fun get(alias: Alias): String =
        registered.getOrPut(alias.identifier) { alias
            .identifier.asString
            ?: generateT()
        }

    fun positionalLabel(position: Int): String? = positionalName(position)
}