package io.koalaql.query.fluent

import io.koalaql.expr.Ordinal
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.built.FullQueryBuilder
import io.koalaql.values.ResultRow

interface UnionedOrderable: Unionable<ResultRow>, UnionedOffsetable {
    private class OrderBy(
        val of: UnionedOrderable,
        val ordinals: List<Ordinal<*>>
    ): UnionedOffsetable {
        override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder? {
            orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): UnionedOffsetable =
        OrderBy(this, ordinals.asList())
}