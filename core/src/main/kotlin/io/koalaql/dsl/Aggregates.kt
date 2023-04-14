package io.koalaql.dsl

import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.fluent.FilterableExpr
import java.math.BigDecimal

fun <T : Any> distinct(expr: Expr<T>): OrderableAggregatable<T> =
    DistinctAggregatable(expr)

fun <T : Any> avg(aggregatable: Aggregatable<T>): FilterableExpr<BigDecimal> =
    GroupedOperationExpr(StandardOperationType.AVG, listOf(BuiltAggregatable.from(aggregatable)))

fun count(aggregatable: Aggregatable<*>): FilterableExpr<Int> =
    GroupedOperationExpr(StandardOperationType.COUNT, listOf(BuiltAggregatable.from(aggregatable)))

fun count(): FilterableExpr<Int> =
    GroupedOperationExpr(StandardOperationType.COUNT, listOf())

fun <T : Any> max(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(StandardOperationType.MAX, listOf(BuiltAggregatable.from(aggregatable)))

fun <T : Any> min(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(StandardOperationType.MIN, listOf(BuiltAggregatable.from(aggregatable)))

fun <T : Any> stddevPop(aggregatable: Aggregatable<T>): FilterableExpr<BigDecimal> =
    GroupedOperationExpr(StandardOperationType.STDDEV_POP, listOf(BuiltAggregatable.from(aggregatable)))

fun <T : Any> sum(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(StandardOperationType.SUM, listOf(BuiltAggregatable.from(aggregatable)))

fun <T : Any> varPop(aggregatable: Aggregatable<T>): FilterableExpr<BigDecimal> =
    GroupedOperationExpr(StandardOperationType.VAR_POP, listOf(BuiltAggregatable.from(aggregatable)))