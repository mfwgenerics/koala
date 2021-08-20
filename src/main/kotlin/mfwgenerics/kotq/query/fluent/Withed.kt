package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.dsl.Insert
import mfwgenerics.kotq.dsl.Inserted
import mfwgenerics.kotq.dsl.Queryable
import mfwgenerics.kotq.query.built.BuildsIntoInsert

interface Withed: BuildsIntoInsert, Joinable {
    fun insert(queryable: Queryable): Inserted =
        Insert(this, queryable.buildQuery())
}