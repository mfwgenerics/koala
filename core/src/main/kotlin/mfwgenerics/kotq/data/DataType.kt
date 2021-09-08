package mfwgenerics.kotq.data

import kotlin.reflect.KClass

abstract class DataType<F : Any, T : Any> {
    abstract val type: KClass<T>
    abstract val dataType: UnmappedDataType<F>

    abstract val mapping: TypeMapping<F, T>?

    abstract fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<F, R>

    inline fun <reified R : Any> map(noinline to: (T) -> R, noinline from: (R) -> T): DataType<F, R> {
        return map(object : TypeMapping<T, R> {
            override val type: KClass<R> = R::class
            override fun convert(value: T): R = to(value)
            override fun unconvert(value: R): T = from(value)
        })
    }
}