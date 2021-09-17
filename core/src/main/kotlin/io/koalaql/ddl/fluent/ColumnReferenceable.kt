package io.koalaql.ddl.fluent

import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.built.BuildsIntoColumnDef
import io.koalaql.ddl.built.BuiltColumnDef

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