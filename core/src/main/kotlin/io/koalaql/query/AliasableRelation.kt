package io.koalaql.query

interface AliasableRelation: RelationBuilder {
    infix fun as_(alias: Alias): Aliased
}