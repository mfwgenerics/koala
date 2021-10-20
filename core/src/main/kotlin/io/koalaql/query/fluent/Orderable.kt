package io.koalaql.query.fluent

import io.koalaql.expr.Ordinal
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.QueryBodyBuilder

interface Orderable: Offsetable {
    private class OrderBy(
        val of: Orderable,
        val ordinals: List<Ordinal<*>>
    ): Offsetable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder? {
            orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Offsetable =
        OrderBy(this, ordinals.asList())
}