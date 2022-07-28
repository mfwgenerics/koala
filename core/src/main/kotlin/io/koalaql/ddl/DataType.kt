package io.koalaql.ddl

import kotlin.reflect.KClass

sealed class DataType<F : Any, T : Any> {
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

    fun <E : Enum<E>> mapToEnum(
        type: KClass<E>,
        keySelector: (E) -> T
    ): DataType<F, E> {
        val enumByKey = type.java.enumConstants
            .associateBy(keySelector)

        return map(object : TypeMapping<T, E> {
            override val type: KClass<E> = type
            override fun convert(value: T): E = enumByKey.getValue(value)
            override fun unconvert(value: E): T = keySelector(value)
        })
    }

    inline fun <reified E : Enum<E>> mapToEnum(
        noinline keySelector: (E) -> T
    ) = mapToEnum(E::class, keySelector)
}