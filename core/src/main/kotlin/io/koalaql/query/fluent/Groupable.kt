package io.koalaql.query.fluent

import io.koalaql.expr.Expr
import io.koalaql.query.built.QueryBodyBuilder
import io.koalaql.query.built.BuiltQueryBody

interface Groupable: Windowable, Orderable, QueryBodyBuilder {
    private class GroupBy(
        val of: Groupable,
        val on: List<Expr<*>>
    ): Havingable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder {
            groupBy = on
            return of
        }
    }

    fun groupBy(vararg exprs: Expr<*>): Havingable =
        GroupBy(this, exprs.asList())
}