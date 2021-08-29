package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoSelect {
    fun buildSelect(): BuiltSelectQuery =
        unfoldBuilder(BuiltSelectQuery()) { buildIntoSelect(it) }
            .apply { buildSelection() }

    fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect?
}