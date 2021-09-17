package io.koalaql.ddl

import io.koalaql.data.DataType

data class BaseColumnType<T : Any>(
    override val mappedType: DataType<*, T>
): ColumnType<T>