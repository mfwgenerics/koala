package io.koalaql.ddl

data class BaseColumnType<T : Any>(
    override val mappedType: DataType<*, T>
): ColumnType<T>