package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody

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