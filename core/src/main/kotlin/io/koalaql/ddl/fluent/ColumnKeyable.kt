package io.koalaql.ddl.fluent

import io.koalaql.ddl.IndexType
import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.built.ColumnDefBuilder

interface ColumnKeyable<T : Any>: ColumnReferenceable<T> {
    interface Nullable<T : Any>: ColumnReferenceable.Nullable<T> {
        private class Keyed<T : Any>(
            val lhs: Nullable<T>,
            val type: IndexType
        ): ColumnReferenceable.Nullable<T> {
            override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
                markedAsKey = type
                return lhs
            }
        }

        fun primaryKey(): ColumnReferenceable.Nullable<T> = Keyed(this, IndexType.PRIMARY)
        fun uniqueKey(): ColumnReferenceable.Nullable<T> = Keyed(this, IndexType.UNIQUE)
    }

    private class Keyed<T : Any>(
        val lhs: ColumnKeyable<T>,
        val type: IndexType
    ): ColumnReferenceable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
            markedAsKey = type
            return lhs
        }
    }

    fun primaryKey(): ColumnReferenceable<T> = Keyed(this, IndexType.PRIMARY)
    fun uniqueKey(): ColumnReferenceable<T> = Keyed(this, IndexType.UNIQUE)
}