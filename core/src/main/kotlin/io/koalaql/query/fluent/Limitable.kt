package io.koalaql.query.fluent

import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Limitable: Lockable {
    private class Limit(
        val of: Limitable,
        val rows: Int
    ): Lockable {
        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody? {
            out.limit = rows

            return of
        }
    }

    fun limit(rows: Int): Lockable = Limit(this, rows)
}