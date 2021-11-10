package io.koalaql.query.fluent

import io.koalaql.expr.Ordinal
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.values.ResultRow

interface UnionedOrderable: Unionable<ResultRow>, UnionedOffsetable {
    private class OrderBy(
        val of: UnionedOrderable,
        val ordinals: List<Ordinal<*>>
    ): UnionedOffsetable {
        override fun BuiltQuery.buildInto(): QueryBuilder? {
            orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): UnionedOffsetable =
        OrderBy(this, ordinals.asList())
}