package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.expr.fluent.FilterableExpr

fun <T : Any> distinct(expr: Expr<T>): OrderableAggregatable<T> =
    DistinctAggregatable(expr)

fun <T : Any> max(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(GroupedOperationType.MAX, listOf(aggregatable.buildAggregatable()))

fun <T : Any> sum(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(GroupedOperationType.SUM, listOf(aggregatable.buildAggregatable()))