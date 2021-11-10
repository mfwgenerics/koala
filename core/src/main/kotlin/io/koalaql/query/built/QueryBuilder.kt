package io.koalaql.query.built

interface QueryBuilder {
    fun BuiltQuery.buildInto(): QueryBuilder?
}