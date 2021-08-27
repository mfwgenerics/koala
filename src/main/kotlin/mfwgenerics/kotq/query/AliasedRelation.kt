package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectionBuilder
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.query.fluent.Withable

interface AliasedRelation: Withable, SelectArgument {
    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.fromRelation(buildQueryRelation())
    }

    fun buildQueryRelation(): BuiltRelation

    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.relation = buildQueryRelation()
        return null
    }

    override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
        out.relation = buildQueryRelation()
        return null
    }
}