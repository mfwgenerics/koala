package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltRelation

object Tableless: AliasedRelation {
    override fun buildQueryRelation(): BuiltRelation = BuiltRelation(
        relation = EmptyRelation,
        explicitAlias = null
    )
}