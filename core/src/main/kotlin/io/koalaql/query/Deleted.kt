package io.koalaql.query

import io.koalaql.query.built.BuiltWith
import io.koalaql.query.fluent.BuildsIntoDelete
import io.koalaql.query.fluent.Withable

interface Deleted: BuildsIntoDelete, Withable<BuildsIntoDelete> {
    override fun with(type: WithType, queries: List<BuiltWith>) = BuildsIntoDelete {
        withType = type
        withs = queries

        this@Deleted
    }
}