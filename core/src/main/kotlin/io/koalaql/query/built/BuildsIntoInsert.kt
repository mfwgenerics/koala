package io.koalaql.query.built

import io.koalaql.unfoldBuilder

interface BuildsIntoInsert {
    fun buildInsert(): BuiltInsert =
        unfoldBuilder(BuiltInsert()) { buildIntoInsert(it) }

    fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert?
}