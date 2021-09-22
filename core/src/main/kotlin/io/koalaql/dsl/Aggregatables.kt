package io.koalaql.dsl

import io.koalaql.expr.*
import io.koalaql.expr.fluent.FilterableExpr

fun <T : Any> distinct(expr: Expr<T>): OrderableAggregatable<T> =
    DistinctAggregatable(expr)

fun <T : Any> avg(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.AVG, listOf(aggregatable.buildAggregatable()))

fun count(aggregatable: Aggregatable<*>): FilterableExpr<Int> =
    GroupedOperationExpr(OperationType.COUNT, listOf(aggregatable.buildAggregatable()))

fun count(): FilterableExpr<Int> =
    GroupedOperationExpr(OperationType.COUNT, listOf())

fun <T : Any> max(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.MAX, listOf(aggregatable.buildAggregatable()))

fun <T : Any> min(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.MIN, listOf(aggregatable.buildAggregatable()))

fun <T : Any> stddevPop(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.STDDEV_POP, listOf(aggregatable.buildAggregatable()))

fun <T : Any> sum(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.SUM, listOf(aggregatable.buildAggregatable()))

fun <T : Any> varPop(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(OperationType.VAR_POP, listOf(aggregatable.buildAggregatable()))