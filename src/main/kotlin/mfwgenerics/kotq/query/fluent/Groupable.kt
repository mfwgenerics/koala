package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Groupable: Windowable, Orderable, BuildsIntoSelect {
    private class GroupBy(
        val of: Groupable,
        val on: List<Expr<*>>
    ): Havingable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.groupBy = on
            return of
        }
    }

    fun groupBy(vararg exprs: Expr<*>): Havingable =
        GroupBy(this, exprs.asList())
}