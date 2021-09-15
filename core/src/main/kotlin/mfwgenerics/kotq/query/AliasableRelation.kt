package mfwgenerics.kotq.query

interface AliasableRelation: AliasedRelation {
    infix fun as_(alias: Alias): Aliased
}