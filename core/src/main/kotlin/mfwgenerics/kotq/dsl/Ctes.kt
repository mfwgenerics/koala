package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.query.*

fun cte(identifier: String? = null): Cte =
    Cte(IdentifierName(identifier))

infix fun Cte.as_(queryable: Subqueryable): CtedQueryable =
    CtedQueryable(this, queryable)

infix fun Alias.as_(queryable: Subqueryable): AliasedCtedQueryable {
    val cte = Cte(identifier)

    return AliasedCtedQueryable(
        cte as_ this,
        cte as_ queryable
    )
}
