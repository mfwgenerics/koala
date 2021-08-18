package mfwgenerics.kotq

class AliasedQueryable(
    val alias: Alias,
    val queryable: Queryable
)

infix fun Alias.`as`(queryable: Queryable): AliasedQueryable =
    AliasedQueryable(this, queryable)