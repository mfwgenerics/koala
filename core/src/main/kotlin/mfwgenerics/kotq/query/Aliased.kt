package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.built.BuiltRelation

class Aliased(
    private val of: Relation,
    private val alias: Alias
): AliasedRelation {
    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(of, alias)

    operator fun <T : Any> get(reference: Reference<T>) = alias[reference]
}