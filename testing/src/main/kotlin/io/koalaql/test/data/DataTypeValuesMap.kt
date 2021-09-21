package io.koalaql.test.data

import io.koalaql.data.UnmappedDataType

class DataTypeValuesMap {
    private val mapping = hashMapOf<UnmappedDataType<*>, List<Any>>()

    @Suppress("unchecked_cast")
    operator fun <T : Any> get(type: UnmappedDataType<T>): List<T> =
        mapping[type] as List<T>

    operator fun <T : Any> set(type: UnmappedDataType<T>, values: List<T>) {
        mapping[type] = values
    }

    fun remove(type: UnmappedDataType<*>) {
        mapping.remove(type)
    }

    @Suppress("unchecked_cast")
    fun entries(): List<DataTypeWithValues<*>> = mapping.map { (k, v) ->
        DataTypeWithValues(k as UnmappedDataType<Any>, v)
    }
}