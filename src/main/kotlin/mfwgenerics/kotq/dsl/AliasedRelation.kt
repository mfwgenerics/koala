package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.built.*
import mfwgenerics.kotq.query.fluent.Withable

interface AliasedRelation: Withable, NamedExprs {
    fun buildQueryRelation(): BuiltRelation

    override fun buildIntoWhere(out: BuiltWhere): BuildsIntoWhereQuery? {
        out.relation = buildQueryRelation()
        return null
    }

    override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
        out.relation = buildQueryRelation()
        return null
    }
}