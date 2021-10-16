package io.koalaql.query.fluent

import io.koalaql.expr.Expr
import io.koalaql.query.RelationBuilder
import io.koalaql.query.JoinType
import io.koalaql.query.built.QueryBodyBuilder
import io.koalaql.query.built.BuiltJoin
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.BuiltRelation

interface Joinable: Whereable {
    private class Join(
        val of: Joinable,
        val type: JoinType,
        val to: RelationBuilder,
        val on: Expr<Boolean>
    ): Joinable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder? {
            joins.add(BuiltJoin(
                type = type,
                to = BuiltRelation.from(to),
                on = on
            ))

            return of
        }
    }

    fun join(type: JoinType, to: RelationBuilder, on: Expr<Boolean>): Joinable =
        Join(this, type, to, on)

    fun innerJoin(to: RelationBuilder, on: Expr<Boolean>): Joinable =
        join(JoinType.INNER, to, on)

    fun leftJoin(to: RelationBuilder, on: Expr<Boolean>): Joinable =
        join(JoinType.LEFT, to, on)

    fun rightJoin(to: RelationBuilder, on: Expr<Boolean>): Joinable =
        join(JoinType.RIGHT, to, on)

    fun outerJoin(to: RelationBuilder, on: Expr<Boolean>) =
        join(JoinType.OUTER, to, on)
}