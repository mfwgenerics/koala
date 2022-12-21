package io.koalaql.query

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltWith

interface WithOperand {
    fun buildCtedQueryable(): CtedQueryable

    fun buildWith(): BuiltWith {
        val cted = buildCtedQueryable()

        return BuiltWith(
            cted.cte,
            with (cted.queryable) { BuilderContext.buildQuery() }
        )
    }
}