package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody

interface Limitable: Lockable {
    private class Limit(
        val of: Limitable,
        val rows: Int
    ): Lockable {
        override fun buildIntoSelect(out: BuiltQueryBody): BuildsIntoQueryBody? {
            out.limit = rows

            return of
        }
    }

    fun limit(rows: Int): Lockable = Limit(this, rows)
}