package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.AliasedRelation
import mfwgenerics.kotq.dsl.JoinType
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.built.BuildsIntoWhereQuery
import mfwgenerics.kotq.query.built.BuiltJoin
import mfwgenerics.kotq.query.built.BuiltWhere

interface Joinable: Whereable {
    private class Join(
        val of: Joinable,
        val type: JoinType,
        val to: AliasedRelation,
        val on: Expr<Boolean>
    ): Joinable {
        override fun buildIntoWhere(out: BuiltWhere): BuildsIntoWhereQuery? {
            out.joins.add(BuiltJoin(
                type = type,
                to = to.buildQueryRelation(),
                on = on
            ))

            return of
        }
    }

    fun join(type: JoinType, to: AliasedRelation, on: Expr<Boolean>): Joinable =
        Join(this, type, to, on)

    fun innerJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.INNER, to, on)

    fun leftJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.LEFT, to, on)

    fun rightJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.RIGHT, to, on)

    fun outerJoin(to: AliasedRelation, on: Expr<Boolean>) =
        join(JoinType.OUTER, to, on)
}