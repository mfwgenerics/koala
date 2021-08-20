package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Orderable: Offsetable {
    private class OrderBy(
        val of: Orderable,
        val ordinals: List<Ordinal<*>>
    ): Offsetable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.orderBy = ordinals
            return of
        }
    }

    fun orderBy(vararg ordinals: Ordinal<*>): Offsetable =
        OrderBy(this, ordinals.asList())
}