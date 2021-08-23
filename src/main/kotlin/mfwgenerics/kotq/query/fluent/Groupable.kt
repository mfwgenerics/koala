package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoSelectBody
import mfwgenerics.kotq.query.built.BuildsIntoWhereQuery
import mfwgenerics.kotq.query.built.BuiltSelectBody

interface Groupable: Windowable, Orderable, BuildsIntoWhereQuery {
    private class GroupBy(
        val of: Groupable,
        val on: List<Expr<*>>
    ): Havingable {
        override fun buildIntoSelectBody(out: BuiltSelectBody): BuildsIntoSelectBody? {
            out.groupBy = on
            return of
        }
    }

    fun groupBy(vararg exprs: Expr<*>): Havingable =
        GroupBy(this, exprs.asList())
}