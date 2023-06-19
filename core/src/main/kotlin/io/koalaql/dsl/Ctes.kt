package io.koalaql.dsl

import io.koalaql.identifier.LabelIdentifier
import io.koalaql.query.*

fun cte(identifier: String? = null): Cte =
    Cte(LabelIdentifier(identifier))

infix fun Alias.as_(queryable: Subqueryable<*>): AliasedCtedQueryable {
    val cte = Cte(identifier)

    return AliasedCtedQueryable(
        cte as_ this,
        cte as_ queryable
    )
}
