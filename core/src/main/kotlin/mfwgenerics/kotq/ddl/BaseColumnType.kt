package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.MappedDataType

class BaseColumnType<T : Any>(
    override val mappedType: MappedDataType<*, T>
): ColumnType<T>