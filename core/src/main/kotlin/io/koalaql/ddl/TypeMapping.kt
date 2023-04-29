package io.koalaql.ddl

import kotlin.reflect.KType

interface TypeMapping<F, T : Any> {
    val type: KType

    fun convert(value: F): T
    fun unconvert(value: T): F

    fun <R : Any> then(next: TypeMapping<T, R>): TypeMapping<F, R> = object : TypeMapping<F, R> {
        override val type: KType = next.type

        override fun convert(value: F): R = next.convert(this@TypeMapping.convert(value))
        override fun unconvert(value: R): F = this@TypeMapping.unconvert(next.unconvert(value))
    }
}