package io.koalaql.ddl.fluent

import io.koalaql.ddl.IndexType
import io.koalaql.ddl.built.BuildsIntoColumnDef
import io.koalaql.ddl.built.BuiltColumnDef

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