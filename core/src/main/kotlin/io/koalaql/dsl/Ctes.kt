package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.query.*

fun cte(identifier: String? = null): Cte =
    Cte(IdentifierName(identifier))

infix fun Cte.as_(queryable: Queryable): CtedQueryable =
    CtedQueryable(this, queryable)

infix fun Alias.as_(queryable: Queryable): AliasedCtedQueryable {
    val cte = Cte(identifier)

    return AliasedCtedQueryable(
        cte as_ this,
        cte as_ queryable
    )
}
