package mfwgenerics.kotq.data

import kotlin.reflect.KClass

interface MappedDataType<F : Any, T : Any> {
    val type: KClass<T>
    val dataType: DataType<F>

    fun convert(from: F): T
    fun unconvert(from: T): F
}