package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.built.FullQueryBuilder

interface UnionedLimitable: UnionedQueryable {
    private class Limit(
        val of: UnionedLimitable,
        val rows: Int
    ): UnionedQueryable {
        override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder? {
            limit = rows
            return of
        }
    }

    fun limit(rows: Int): UnionedQueryable = Limit(this, rows)
}