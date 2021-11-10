package io.koalaql.query.built

interface FullQueryBuilder {
    fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder?
}