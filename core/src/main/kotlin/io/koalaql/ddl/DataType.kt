package io.koalaql.ddl

import kotlin.reflect.KClass

abstract class DataType<F : Any, T : Any> {
    abstract val type: KClass<T>
    abstract val dataType: UnmappedDataType<F>

    abstract val mapping: TypeMapping<F, T>?

    abstract fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<F, R>

    fun <R : Any> map(type: KClass<R>, to: (T) -> R, from: (R) -> T): DataType<F, R> = map(object : TypeMapping<T, R> {
        override val type: KClass<R> = type
        override fun convert(value: T): R = to(value)
        override fun unconvert(value: R): T = from(value)
    })

    inline fun <reified R : Any> map(noinline to: (T) -> R, noinline from: (R) -> T): DataType<F, R> =
        map(R::class, to, from)
}