package mfwgenerics.kotq.ddl.fluent

import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.BuildsIntoColumnDef
import mfwgenerics.kotq.ddl.built.BuiltColumnDef

interface ColumnReferenceable<T : Any>: ColumnDefinition<T> {
    private class Referenced<T : Any>(
        val lhs: ColumnReferenceable<T>,
        val column: TableColumn<T>
    ): ColumnDefinition<T> {
        override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
            out.references = column
            return lhs
        }
    }

    fun reference(column: TableColumn<T>): ColumnDefinition<T> =
        Referenced(this, column)
}