package io.koalaql.sql

import io.koalaql.ddl.DataType
import kotlin.reflect.KType

class TypeMappings(
    private val mappings: Map<KType, DataType<*, *>>
) {
    operator fun get(type: KType): DataType<*, *>? =
        mappings[type]
}