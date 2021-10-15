package io.koalaql.query.fluent

import io.koalaql.expr.Expr
import io.koalaql.query.AliasedRelation
import io.koalaql.query.JoinType
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltJoin
import io.koalaql.query.built.BuiltQueryBody

interface Joinable: Whereable {
    private class Join(
        val of: Joinable,
        val type: JoinType,
        val to: AliasedRelation,
        val on: Expr<Boolean>
    ): Joinable {
        override fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody? {
            joins.add(BuiltJoin(
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