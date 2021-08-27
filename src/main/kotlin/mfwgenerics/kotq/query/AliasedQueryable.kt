package mfwgenerics.kotq.query

class AliasedQueryable(
    val alias: Cte,
    val queryable: Subqueryable
)