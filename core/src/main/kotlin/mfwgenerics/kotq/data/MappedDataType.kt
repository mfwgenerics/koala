package mfwgenerics.kotq.data

import kotlin.reflect.KClass

abstract class MappedDataType<F : Any, T : Any> {
    abstract val type: KClass<T>
    abstract val dataType: DataType<F>

    abstract fun convert(value: F): T
    abstract fun unconvert(value: T): F

    fun <R : Any> map(type: KClass<R>, to: (T) -> R, from: (R) -> T): MappedDataType<F, R> =
        RemappedDataType(type, this, to, from)

    inline fun <reified R : Any> map(noinline to: (T) -> R, noinline from: (R) -> T): MappedDataType<F, R> =
        map(R::class, to, from)
}