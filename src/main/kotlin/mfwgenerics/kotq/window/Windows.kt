package mfwgenerics.kotq.window

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.literal

fun all(): Partitionable = GlobalWindow

fun unbounded(): FrameRangeMarker<Nothing> =
    Unbounded

fun currentRow(): FrameRangeMarker<Nothing> =
    CurrentRow

fun <T : Any> preceding(offset: Expr<T>): FrameRangeMarker<Expr<T>> =
    Preceding(offset)
inline fun <reified T : Any> preceding(offset: T): FrameRangeMarker<Expr<T>> =
    Preceding(literal(offset))

fun <T : Any> following(offset: Expr<T>): FrameRangeMarker<Expr<T>> =
    Following(offset)
inline fun <reified T : Any> following(offset: T): FrameRangeMarker<Expr<T>> =
    Following(literal(offset))