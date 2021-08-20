package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Offsetable: Limitable {
    private class Offset(
        val of: Offsetable,
        val rows: Int
    ): Limitable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.offset = rows

            return of
        }
    }

    fun offset(rows: Int): Limitable =
        Offset(this, rows)
}