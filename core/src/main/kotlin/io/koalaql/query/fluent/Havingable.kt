package io.koalaql.query.fluent

import io.koalaql.dsl.and
import io.koalaql.expr.Expr
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Havingable: Windowable {
    private class Having(
        val of: Havingable,
        val having: Expr<Boolean>
    ): Havingable {
        override fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody {
            having = having?.and(this@Having.having)?:this@Having.having
            return of
        }
    }

    fun having(having: Expr<Boolean>): Havingable = Having(this, having)
}