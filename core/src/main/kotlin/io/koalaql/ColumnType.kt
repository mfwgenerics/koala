package io.koalaql

import kotlin.reflect.KClass

sealed class ColumnType<T : Any>(
    val type: KClass<T>
) {
    object INT : ColumnType<Int>(Int::class)
}