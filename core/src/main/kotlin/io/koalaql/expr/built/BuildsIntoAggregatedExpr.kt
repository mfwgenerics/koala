package io.koalaql.expr.built

interface BuildsIntoAggregatedExpr {
    fun BuiltAggregatedExpr.buildIntoAggregatedExpr(): BuildsIntoAggregatedExpr?
}