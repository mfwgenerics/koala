package io.koalaql.query

class CtedQueryable(
    val cte: Cte,
    val queryable: Subqueryable<*>
): WithOperand, AliasableRelation by cte {
    override fun buildCtedQueryable(): CtedQueryable = this
}