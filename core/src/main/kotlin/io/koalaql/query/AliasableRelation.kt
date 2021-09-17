package io.koalaql.query

interface AliasableRelation: AliasedRelation {
    infix fun as_(alias: Alias): Aliased
}