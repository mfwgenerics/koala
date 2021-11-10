package io.koalaql.query

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectionBuilder
import io.koalaql.query.built.*
import io.koalaql.query.fluent.Withed

interface RelationBuilder: Withed, SelectArgument {
    override fun SelectionBuilder.buildIntoSelection() {
        fromRelation(BuiltRelation.from(this@RelationBuilder))
    }

    fun BuiltRelation.buildIntoRelation()

    override fun BuiltQueryBody.buildInto(): BuildsIntoQueryBody? {
        relation = BuiltRelation.from(this@RelationBuilder)
        return null
    }

    override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
        relation = BuiltRelation.from(this@RelationBuilder)
        return null
    }
}