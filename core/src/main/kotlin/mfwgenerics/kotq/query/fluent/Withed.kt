package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.values
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltInsert
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.values.ValuesRow

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

    fun insert(row: ValuesRow): OnConflictable =
        insert(values(row))
}