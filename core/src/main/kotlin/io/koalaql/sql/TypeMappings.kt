package io.koalaql.sql

import io.koalaql.ddl.MappedDataType
import kotlin.reflect.KClass

class TypeMappings(
    private val mappings: Map<KClass<*>, MappedDataType<*, *>>
) {
    @Suppress("unchecked_cast")
    operator fun <T : Any> get(type: KClass<T>): MappedDataType<*, T>? =
        mappings[type] as? MappedDataType<*, T>
}