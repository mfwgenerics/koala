package io.koalaql.query.fluent

import io.koalaql.dsl.values
import io.koalaql.query.Subqueryable
import io.koalaql.query.built.BuildsIntoInsert
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.values.ValuesRow

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