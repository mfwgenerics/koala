package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder

interface UnionedLimitable: UnionedQueryable {
    private class Limit(
        val of: UnionedLimitable,
        val rows: Int
    ): UnionedQueryable {
        override fun BuiltQuery.buildInto(): QueryBuilder? {
            limit = rows
            return of
        }
    }

    fun limit(rows: Int): UnionedQueryable = Limit(this, rows)
}