package io.koalaql.query.fluent

import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Offsetable: Limitable {
    private class Offset(
        val of: Offsetable,
        val rows: Int
    ): Limitable {
        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody? {
            out.offset = rows

            return of
        }
    }

    fun offset(rows: Int): Limitable =
        Offset(this, rows)
}