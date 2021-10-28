package io.koalaql.query.fluent

import io.koalaql.dsl.values
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.built.InsertBuilder
import io.koalaql.values.ValuesRow

interface Withed: InsertBuilder, Joinable {
    private class Insert(
        val ignore: Boolean,
        val of: Withed,
        val query: BuiltSubquery
    ): OnConflictable {
        override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
            ignore = this@Insert.ignore
            query = this@Insert.query
            return of
        }
    }

    fun insert(queryable: Queryable): OnConflictable =
        Insert(false, this, queryable.buildQuery())

    fun insert(row: ValuesRow): OnConflictable =
        insert(values(row))

    fun insertIgnore(queryable: Queryable): OnConflictable =
        Insert(true, this, queryable.buildQuery())

    fun insertIgnore(row: ValuesRow): OnConflictable =
        insertIgnore(values(row))
}