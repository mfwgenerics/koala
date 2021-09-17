package io.koalaql.data

import kotlin.reflect.KClass

interface TypeMapping<F, T : Any> {
    val type: KClass<T>

    fun convert(value: F): T
    fun unconvert(value: T): F

    fun <R : Any> then(next: TypeMapping<T, R>): TypeMapping<F, R> = object : TypeMapping<F, R> {
        override val type: KClass<R> = next.type

        override fun convert(value: F): R = next.convert(this@TypeMapping.convert(value))
        override fun unconvert(value: R): F = this@TypeMapping.unconvert(next.unconvert(value))
    }
}