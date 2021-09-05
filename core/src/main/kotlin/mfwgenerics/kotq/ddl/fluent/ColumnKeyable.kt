package mfwgenerics.kotq.ddl.fluent

import mfwgenerics.kotq.ddl.IndexType
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.BuildsIntoColumnDef
import mfwgenerics.kotq.ddl.built.BuiltColumnDef

interface ColumnKeyable<T : Any>: ColumnReferenceable<T> {
    private class Keyed<T : Any>(
        val lhs: ColumnKeyable<T>,
        val type: IndexType
    ): ColumnReferenceable<T> {
        override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
            out.markedAsKey = type
            return lhs
        }
    }

    fun primaryKey(): ColumnReferenceable<T> = Keyed(this, IndexType.PRIMARY)
    fun uniqueKey(): ColumnReferenceable<T> = Keyed(this, IndexType.UNIQUE)
}