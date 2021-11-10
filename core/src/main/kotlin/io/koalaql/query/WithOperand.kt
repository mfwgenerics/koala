package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltWith

interface WithOperand {
    fun buildCtedQueryable(): CtedQueryable

    fun buildWith(): BuiltWith {
        val cted = buildCtedQueryable()

        return BuiltWith(
            cted.cte,
            BuiltQuery.from(cted.queryable)
        )
    }
}