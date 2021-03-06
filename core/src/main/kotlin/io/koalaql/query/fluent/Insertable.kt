package io.koalaql.query.fluent

import io.koalaql.dsl.values
import io.koalaql.query.Queryable
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.InsertBuilder
import io.koalaql.values.ResultRow
import io.koalaql.values.ValuesRow

interface Insertable: InsertBuilder, Joinable {
    private class Insert(
        val ignore: Boolean,
        val of: Insertable,
        val query: BuiltQuery
    ): OnConflictable {
        override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
            ignore = this@Insert.ignore
            query = this@Insert.query
            return of
        }
    }

    fun insert(queryable: Queryable<ResultRow>): OnConflictable =
        Insert(false, this, with (queryable) { BuilderContext.buildQuery() })

    fun insert(row: ValuesRow): OnConflictable =
        insert(values(row))

    fun insertIgnore(queryable: Queryable<ResultRow>): OnConflictable =
        Insert(true, this, with (queryable) { BuilderContext.buildQuery() })

    fun insertIgnore(row: ValuesRow): OnConflictable =
        insertIgnore(values(row))
}