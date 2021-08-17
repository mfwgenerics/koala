package mfwgenerics.kotq.window

sealed interface FrameRangeMarker<out T>

object Unbounded: FrameRangeMarker<Nothing>
object CurrentRow: FrameRangeMarker<Nothing>

data class Preceding<T>(val rows: T): FrameRangeMarker<T>
data class Following<T>(val rows: T): FrameRangeMarker<T>
