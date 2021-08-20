package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.built.BuiltRelation

class Aliased(
    val of: Relation,
    val alias: Alias
): AliasedRelation, NamedExprs by of {
    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(of, alias)
}