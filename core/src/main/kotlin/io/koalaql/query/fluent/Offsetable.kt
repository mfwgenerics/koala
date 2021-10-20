package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.QueryBodyBuilder

interface Offsetable: Limitable {
    private class Offset(
        val of: Offsetable,
        val rows: Int
    ): Limitable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder? {
            offset = rows

            return of
        }
    }

    fun offset(rows: Int): Limitable =
        Offset(this, rows)
}