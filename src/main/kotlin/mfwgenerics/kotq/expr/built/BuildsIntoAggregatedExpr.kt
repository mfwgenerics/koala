package mfwgenerics.kotq.expr.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoAggregatedExpr {
    fun buildAggregated(): BuiltAggregatedExpr =
        unfoldBuilder(BuiltAggregatedExpr()) { buildIntoGroupExpr(it) }

    fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr?
}