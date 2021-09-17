package io.koalaql.query

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectionBuilder
import io.koalaql.query.built.*
import io.koalaql.query.fluent.Withable

interface AliasedRelation: Withable, SelectArgument {
    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.fromRelation(buildQueryRelation())
    }

    fun buildQueryRelation(): BuiltRelation

    override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody? {
        out.relation = buildQueryRelation()
        return null
    }

    override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
        out.relation = buildQueryRelation()
        return null
    }
}