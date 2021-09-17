package io.koalaql.expr.built

import io.koalaql.unfoldBuilder

interface BuildsIntoAggregatedExpr {
    fun buildAggregated(): BuiltAggregatedExpr =
        unfoldBuilder(BuiltAggregatedExpr()) { buildIntoGroupExpr(it) }

    fun buildIntoGroupExpr(aggregatedExpr: BuiltAggregatedExpr): BuildsIntoAggregatedExpr?
}