package io.koalaql.query.fluent

import io.koalaql.expr.Ordinal
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Orderable: Offsetable {
    private class OrderBy(
        val of: Orderable,
        val ordinals: List<Ordinal<*>>
    ): Offsetable {
        override fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody? {
            orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Offsetable =
        OrderBy(this, ordinals.asList())
}