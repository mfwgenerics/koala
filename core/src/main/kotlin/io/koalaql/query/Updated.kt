package io.koalaql.query

import io.koalaql.query.built.BuiltWith
import io.koalaql.query.fluent.BuildsIntoUpdate
import io.koalaql.query.fluent.Withable

interface Updated: BuildsIntoUpdate, Withable<BuildsIntoUpdate> {
    override fun with(type: WithType, queries: List<BuiltWith>) = BuildsIntoUpdate {
        withType = type
        withs = queries

        this@Updated
    }
}