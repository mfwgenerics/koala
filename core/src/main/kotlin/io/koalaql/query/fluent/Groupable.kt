package io.koalaql.query.fluent

import io.koalaql.expr.Expr
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Groupable: Windowable, Orderable, BuildsIntoQueryBody {
    private class GroupBy(
        val of: Groupable,
        val on: List<Expr<*>>
    ): Havingable {
        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody {
            out.groupBy = on
            return of
        }
    }

    fun groupBy(vararg exprs: Expr<*>): Havingable =
        GroupBy(this, exprs.asList())
}