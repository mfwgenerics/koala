package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.ddl.built.BuildsIntoColumnDef
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.fluent.ColumnIncrementable

interface ColumnType<T : Any>: ColumnIncrementable<T> {
    val mappedType: DataType<*, T>

    override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
        out.columnType = mappedType
        return null
    }
}

