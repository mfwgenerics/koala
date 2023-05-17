package io.koalaql.ddl

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class DataType<F : Any, T : Any> {
    abstract val type: KType
    abstract val dataType: UnmappedDataType<F>

    abstract val mapping: TypeMapping<F, T>?

    abstract fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<F, R>

    fun <R : Any> map(type: KType, to: (T) -> R, from: (R) -> T): DataType<F, R> = map(object : TypeMapping<T, R> {
        override val type: KType = type
        override fun convert(value: T): R = to(value)
        override fun unconvert(value: R): T = from(value)
    })

    inline fun <reified R : Any> map(noinline to: (T) -> R, noinline from: (R) -> T): DataType<F, R> =
        map(typeOf<R>(), to, from)

    fun <E : Enum<E>> mapToEnum(
        type: KType,
        keySelector: (E) -> T
    ): DataType<F, E> {
        @Suppress("unchecked_cast")
        val enumByKey = (type.classifier as KClass<E>).java.enumConstants
            .associateBy(keySelector)

        return map(object : TypeMapping<T, E> {
            override val type: KType = type
            override fun convert(value: T): E = enumByKey.getValue(value)
            override fun unconvert(value: E): T = keySelector(value)
        })
    }

    inline fun <reified E : Enum<E>> mapToEnum(
        noinline keySelector: (E) -> T
    ) = mapToEnum(typeOf<E>(), keySelector)
}