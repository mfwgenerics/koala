package io.koalaql.query

import io.koalaql.expr.AliasedReference
import io.koalaql.expr.AsReference
import io.koalaql.query.built.BuiltRelation

class Aliased(
    private val of: Relation,
    private val alias: Alias
): AliasedRelation, GetsAliasedReference {
    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(of, alias)

    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> = alias[reference]
}