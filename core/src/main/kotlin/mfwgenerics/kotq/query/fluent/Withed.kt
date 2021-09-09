package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltInsert
import mfwgenerics.kotq.query.built.BuiltSubquery

interface Withed: BuildsIntoInsert, Joinable {
    private class Insert(
        val of: Withed,
        val query: BuiltSubquery
    ): OnConflictable {
        override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
            out.query = query
            return of
        }
    }

    fun insert(queryable: Subqueryable): OnConflictable =
        Insert(this, queryable.buildQuery())
}