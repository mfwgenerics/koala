package io.koalaql.ddl.fluent

import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.built.ColumnDefBuilder

interface ColumnReferenceable<T : Any>: ColumnDefinition<T> {
    interface Nullable<T : Any>: ColumnDefinition.Nullable<T> {
        private class Referenced<T : Any>(
            val lhs: Nullable<T>,
            val column: TableColumn<T>
        ): ColumnDefinition.Nullable<T> {
            override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
                references = column
                return lhs
            }
        }

        fun reference(column: TableColumn<T>): ColumnDefinition.Nullable<T> =
            Referenced(this, column)
    }

    private class Referenced<T : Any>(
        val lhs: ColumnReferenceable<T>,
        val column: TableColumn<T>
    ): ColumnDefinition<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
            references = column
            return lhs
        }
    }

    fun reference(column: TableColumn<T>): ColumnDefinition<T> =
        Referenced(this, column)
}