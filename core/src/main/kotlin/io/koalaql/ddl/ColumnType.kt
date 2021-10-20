package io.koalaql.ddl

import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.built.ColumnDefBuilder
import io.koalaql.ddl.fluent.ColumnIncrementable

interface ColumnType<T : Any>: ColumnIncrementable<T> {
    val mappedType: DataType<*, T>

    override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
        columnType = mappedType
        return null
    }
}

