package io.koalaql.sql

import io.koalaql.expr.Reference
import io.koalaql.identifier.*
import io.koalaql.query.Alias
import io.koalaql.query.Cte
import io.koalaql.window.WindowLabel

class NameRegistry(
    private val positionalName: (Int) -> String? = { null }
) {
    private class Names<K>(
        private val prefix: String
    ) {
        private val registered = hashMapOf<K, SqlIdentifier>()
        private var count = 0

        private fun generate() = Unquoted("$prefix${count++}")

        fun get(key: K, identifier: LabelIdentifier?): SqlIdentifier =
            registered.getOrPut(key) {
                when (identifier) {
                    is Named -> identifier
                    null, is Unnamed -> generate()
                }
            }
    }

    private val tables = Names<LabelIdentifier>("T")
    private val columns = Names<Reference<*>>("c")
    private val windows = Names<WindowLabel>("w")

    operator fun get(cte: Cte): SqlIdentifier =
        tables.get(cte.identifier, cte.identifier)

    operator fun get(name: Reference<*>): SqlIdentifier =
        columns.get(name, name.identifier)

    operator fun get(label: WindowLabel): SqlIdentifier =
        windows.get(label, label.identifier)

    operator fun get(alias: Alias): SqlIdentifier =
        tables.get(alias.identifier, alias.identifier)

    fun positionalLabel(position: Int): SqlIdentifier? = positionalName(position)?.let(::Named)
}