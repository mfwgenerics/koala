package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.built.FullQueryBuilder

interface UnionedOffsetable: UnionedLimitable {
    private class Offset(
        val of: UnionedOffsetable,
        val rows: Int
    ): UnionedLimitable {
        override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder? {
            offset = rows
            return of
        }
    }

    fun offset(rows: Int): UnionedLimitable =
        Offset(this, rows)
}