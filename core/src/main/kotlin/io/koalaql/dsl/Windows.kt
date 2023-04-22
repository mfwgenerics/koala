package io.koalaql.dsl

import io.koalaql.expr.Expr
import io.koalaql.expr.GroupedOperationExpr
import io.koalaql.expr.StandardOperationType
import io.koalaql.expr.WindowFunctionExpr
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.fluent.WindowFunction
import io.koalaql.identifier.LabelIdentifier
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
    WindowLabel(LabelIdentifier(identifier))

infix fun WindowLabel.as_(window: Window): LabeledWindow =
    LabeledWindow(window, this)

fun rowNumber(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.ROW_NUMBER, emptyList()))

fun rank(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.RANK, emptyList()))

fun denseRank(): WindowFunction<Long> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.DENSE_RANK, emptyList()))

fun percentRank(): WindowFunction<Double> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.PERCENT_RANK, emptyList()))

fun cumeDist(): WindowFunction<Double> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.CUME_DIST, emptyList()))

fun ntile(buckets: Int): WindowFunction<Int> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.NTILE, listOf(BuiltAggregatable.from(value(buckets)))))

fun <T : Any> lag(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LAG, listOf(
        BuiltAggregatable.from(expr)
    )))

fun <T : Any> lag(expr: Expr<T>, offset: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset))
    )))

fun <T : Any> lag(expr: Expr<T>, offset: Int, default: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset)),
        BuiltAggregatable.from(default)
    )))

fun <T : Any> lead(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LEAD, listOf(
        BuiltAggregatable.from(expr)
    )))

fun <T : Any> lead(expr: Expr<T>, offset: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LEAD, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset))
    )))

fun <T : Any> lead(expr: Expr<T>, offset: Int, default: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LAG, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(offset)),
        BuiltAggregatable.from(default)
    )))

fun <T : Any> firstValue(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.FIRST_VALUE, listOf(BuiltAggregatable.from(expr))))

fun <T : Any> lastValue(expr: Expr<T>): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.LAST_VALUE, listOf(BuiltAggregatable.from(expr))))

fun <T : Any> nthValue(expr: Expr<T>, ordinal: Int): WindowFunction<T> =
    WindowFunctionExpr(GroupedOperationExpr(StandardOperationType.NTH_VALUE, listOf(
        BuiltAggregatable.from(expr),
        BuiltAggregatable.from(value(ordinal))
    )))
