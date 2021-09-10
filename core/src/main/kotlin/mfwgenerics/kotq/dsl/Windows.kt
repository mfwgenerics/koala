package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.window.*
import mfwgenerics.kotq.window.fluent.Partitionable

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