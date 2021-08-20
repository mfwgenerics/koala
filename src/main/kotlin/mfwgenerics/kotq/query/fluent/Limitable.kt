package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Limitable: Lockable {
    private class Limit(
        val of: Limitable,
        val rows: Int
    ): Lockable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.limit = rows

            return of
        }
    }

    fun limit(rows: Int): Lockable = Limit(this, rows)
}