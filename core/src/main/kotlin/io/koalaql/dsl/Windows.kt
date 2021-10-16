package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.expr.Expr
import io.koalaql.expr.GroupedOperationExpr
import io.koalaql.expr.OperationType
import io.koalaql.expr.WindowFunctionExpr
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.fluent.WindowFunction
import io.koalaql.window.*
import io.koalaql.window.fluent.Partitionable

fun all(): Partitionable = GlobalWindow

fun unbounded(): FrameRangeMarker<Nothing> =
    Unbounded

fun currentRow(): FrameRangeMarker<Nothing> =
    CurrentRow

fun <T : Any> preceding(offset: Expr<T>): FrameRangeMarker<Expr<T>> =
    Preceding(offset)
inline fun <reified T : Any> preceding(offset: T): FrameRangeMarker<Expr<T>> =
    Preceding(value(offset))

fun <T : Any> following(offset: Expr<T>): FrameRangeMarker<Expr<T>> =
    Following(offset)
inline fun <reified T : Any> following(offset: T): FrameRangeMarker<Expr<T>> =
    Following(value(offset))

fun window(identifier: String? = null): WindowLabel =
    WindowLabel(IdentifierName(identifier))

infix fun WindowLabel.as_(window: Window): LabeledWindow =
    LabeledWindow(window, this)

fun rowNumber(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.ROW_NUMBER, emptyList()))

fun rank(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.RANK, emptyList()))

fun denseRank(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.DENSE_RANK, emptyList()))

fun percentRank(): WindowFunction<Double> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.PERCENT_RANK, emptyList()))

fun cumeDist(): WindowFunction<Double> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.CUME_DIST, emptyList()))

fun ntile(buckets: Int): WindowFunction<Int> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.NTILE, listOf(BuiltAggregatable.from(value(buckets)))))

fun <T : Any> lag(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LAG, listOf(
        BuiltAggregatable.from(expr)
    )))

fun <T : Any> lag(expr: Expr<T>, offset: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset))
    )))

fun <T : Any> lag(expr: Expr<T>, offset: Int, default: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset)),
        BuiltAggregatable.from(default)
    )))

fun <T : Any> lead(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LEAD, listOf(
        BuiltAggregatable.from(expr)
    )))

fun <T : Any> lead(expr: Expr<T>, offset: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LEAD, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset))
    )))

fun <T : Any> lead(expr: Expr<T>, offset: Int, default: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset)),
        BuiltAggregatable.from(default)
    )))

fun <T : Any> firstValue(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.FIRST_VALUE, listOf(BuiltAggregatable.from(expr))))

fun <T : Any> lastValue(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.LAST_VALUE, listOf(BuiltAggregatable.from(expr))))

fun <T : Any> nthValue(expr: Expr<T>, ordinal: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(OperationType.NTH_VALUE, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(ordinal))
    )))
