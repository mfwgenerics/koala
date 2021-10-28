package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.query.*
import io.koalaql.values.ResultRow

fun cte(identifier: String? = null): Cte =
    Cte(IdentifierName(identifier))

infix fun Cte.as_(queryable: Queryable<ResultRow>): CtedQueryable =
    CtedQueryable(this, queryable)

infix fun Alias.as_(queryable: Queryable<ResultRow>): AliasedCtedQueryable {
    val cte = Cte(identifier)

    return AliasedCtedQueryable(
        cte as_ this,
        cte as_ queryable
    )
}
