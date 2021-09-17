package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.expr.Expr
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