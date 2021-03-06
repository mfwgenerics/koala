package io.koalaql.query.fluent

import io.koalaql.dsl.and
import io.koalaql.expr.Expr
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Whereable: Groupable {
    private class Where(
        val of: Whereable,
        val where: Expr<Boolean>
    ): Whereable {
        override fun BuiltQueryBody.buildInto(): BuildsIntoQueryBody {
            where = where
                ?.let { where -> this@Where.where.and(where) }
                ?:this@Where.where

            return of
        }
    }

    fun where(where: Expr<Boolean>): Whereable = Where(this, where)

    fun whereOptionally(where: Expr<Boolean>?): Whereable = where
        ?.let { where(it) } ?: this
}