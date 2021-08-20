package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName

fun alias(identifier: String? = null): Alias =
    Alias(IdentifierName(identifier))

infix fun Alias.`as`(queryable: Queryable): AliasedQueryable =
    AliasedQueryable(this, queryable)