package io.koalaql.ddl

import io.koalaql.data.DataType
import io.koalaql.ddl.built.BuildsIntoColumnDef
import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.fluent.ColumnIncrementable

interface ColumnType<T : Any>: ColumnIncrementable<T> {
    val mappedType: DataType<*, T>

    override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
        out.columnType = mappedType
        return null
    }
}

