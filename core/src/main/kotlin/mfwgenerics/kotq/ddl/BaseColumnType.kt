package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.DataType

data class BaseColumnType<T : Any>(
    override val mappedType: DataType<*, T>
): ColumnType<T>