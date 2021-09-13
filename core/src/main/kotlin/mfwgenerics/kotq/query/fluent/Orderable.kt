package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody

interface Orderable: Offsetable {
    private class OrderBy(
        val of: Orderable,
        val ordinals: List<Ordinal<*>>
    ): Offsetable {
        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody? {
            out.orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Offsetable =
        OrderBy(this, ordinals.asList())
}