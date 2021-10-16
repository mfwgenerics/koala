package io.koalaql.expr.built

interface AggregatedExprBuilder {
    fun BuiltAggregatedExpr.buildIntoAggregatedExpr(): AggregatedExprBuilder?
}