package io.koalaql.query.built

interface InsertBuilder {
    fun BuiltInsert.buildIntoInsert(): InsertBuilder?
}