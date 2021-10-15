package io.koalaql.query

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectionBuilder
import io.koalaql.query.built.*
import io.koalaql.query.fluent.Withable

interface AliasedRelation: Withable, SelectArgument {
    override fun SelectionBuilder.buildIntoSelection() {
        fromRelation(buildQueryRelation())
    }

    fun buildQueryRelation(): BuiltRelation

    override fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody? {
        relation = buildQueryRelation()
        return null
    }

    override fun BuiltInsert.buildIntoInsert(): BuildsIntoInsert? {
        relation = buildQueryRelation()
        return null
    }
}