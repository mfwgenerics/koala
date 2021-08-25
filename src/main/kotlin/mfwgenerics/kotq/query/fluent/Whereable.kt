package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.and
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Whereable: Groupable {
    private class Where(
        val of: Whereable,
        val where: Expr<Boolean>
    ): Whereable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.where = out.where?.and(where)?:where
            return of
        }
    }

    fun where(where: Expr<Boolean>): Whereable = Where(this, where)
}