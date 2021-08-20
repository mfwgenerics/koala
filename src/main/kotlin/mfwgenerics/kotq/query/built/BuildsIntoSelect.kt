package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoSelect {
    fun buildSelect(): BuiltSelectQuery =
        unfoldBuilder(BuiltSelectQuery()) { buildIntoSelect(it) }

    fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect?
}