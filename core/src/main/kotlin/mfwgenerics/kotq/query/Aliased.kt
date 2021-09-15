package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.AsReference
import mfwgenerics.kotq.query.built.BuiltRelation

class Aliased(
    private val of: Relation,
    private val alias: Alias
): AliasedRelation, GetsAliasedReference {
    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(of, alias)

    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> = alias[reference]
}