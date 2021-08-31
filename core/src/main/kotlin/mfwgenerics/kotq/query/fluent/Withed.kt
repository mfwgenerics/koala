package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.Insert
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuildsIntoInsert

interface Withed: BuildsIntoInsert, Joinable {
    fun insert(queryable: Subqueryable): Inserted =
        Insert(this, queryable.buildQuery())
}