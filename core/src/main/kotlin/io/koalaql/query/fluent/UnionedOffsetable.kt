package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder

interface UnionedOffsetable: UnionedLimitable {
    private class Offset(
        val of: UnionedOffsetable,
        val rows: Int
    ): UnionedLimitable {
        override fun BuiltQuery.buildInto(): QueryBuilder? {
            offset = rows
            return of
        }
    }

    fun offset(rows: Int): UnionedLimitable =
        Offset(this, rows)
}