package io.koalaql.test.data

import io.koalaql.ddl.UnmappedDataType

data class DataTypeWithValues<T : Any>(
    val type: UnmappedDataType<T>,
    val values: List<T>
)
