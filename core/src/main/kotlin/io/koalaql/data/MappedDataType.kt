package io.koalaql.data

import kotlin.reflect.KClass

class MappedDataType<F : Any, T : Any>(
    override val type: KClass<T>,
    override val dataType: UnmappedDataType<F>,
    override val mapping: TypeMapping<F, T>
): DataType<F, T>() {
    override fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<F, R> =
        MappedDataType(mapping.type, dataType, this.mapping.then(mapping))
}