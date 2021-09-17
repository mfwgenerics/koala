package io.koalaql.query

import io.koalaql.query.built.BuiltRelation

object Tableless: AliasedRelation {
    override fun buildQueryRelation(): BuiltRelation = BuiltRelation(
        relation = EmptyRelation,
        explicitAlias = null
    )
}