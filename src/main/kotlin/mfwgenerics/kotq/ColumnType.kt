package mfwgenerics.kotq

sealed class ColumnType<T : Any> {
    object INT : ColumnType<Int>()
}