package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Statementable
import mfwgenerics.kotq.dsl.Relvar
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

    /* Relation rather than Table e.g. self join delete may delete by alias */
    fun delete(vararg relations: Relvar): Statementable =
        TODO()
}