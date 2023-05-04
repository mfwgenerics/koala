package io.koalaql.sql

import io.koalaql.ddl.MappedDataType
import kotlin.reflect.KType

class TypeMappings(
    private val mappings: Map<KType, MappedDataType<*, *>>
) {
    @Suppress("unchecked_cast")
    operator fun <T : Any> get(type: KType): MappedDataType<*, T>? =
        mappings[type] as? MappedDataType<*, T>
}