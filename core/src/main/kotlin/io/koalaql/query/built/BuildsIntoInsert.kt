package io.koalaql.query.built

import io.koalaql.unfoldBuilder

interface BuildsIntoInsert {
    fun buildInsert(): BuiltInsert =
        unfoldBuilder(BuiltInsert()) { it.buildIntoInsert() }

    fun BuiltInsert.buildIntoInsert(): BuildsIntoInsert?
}