package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltRelation

class Aliased(
    val of: Relation,
    val alias: Alias
): AliasedRelation {
    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(of, alias)
}