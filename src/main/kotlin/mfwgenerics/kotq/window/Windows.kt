package mfwgenerics.kotq.window

fun all(): Partitionable = GlobalWindow

fun unbounded(): FrameRangeMarker<Nothing> =
    Unbounded

fun currentRow(): FrameRangeMarker<Nothing> =
    CurrentRow

fun <T> preceding(offset: T): FrameRangeMarker<T> =
    Preceding(offset)

fun <T> following(offset: T): FrameRangeMarker<T> =
    Following(offset)