package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.and
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoWhereQuery
import mfwgenerics.kotq.query.built.BuiltWhere

interface Whereable: Groupable {
    private class Where(
        val of: Whereable,
        val where: Expr<Boolean>
    ): Whereable {
        override fun buildIntoWhere(out: BuiltWhere): BuildsIntoWhereQuery? {
            out.where = out.where?.and(where)?:where
            return of
        }
    }

    fun where(where: Expr<Boolean>): Whereable = Where(this, where)
}