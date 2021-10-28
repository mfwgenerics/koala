package io.koalaql.query

import io.koalaql.values.ResultRow

class CtedQueryable(
    val cte: Cte,
    val queryable: Queryable<ResultRow>
): WithOperand, AliasableRelation by cte {
    override fun buildCtedQueryable(): CtedQueryable = this
}