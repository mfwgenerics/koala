package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.built.ColumnDefBuilder

interface ColumnNullable<T : Any>: ColumnDefaultable<T> {
    private class Nulled<T : Any>(
        val lhs: ColumnNullable<T>
    ): ColumnDefaultable.Nullable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
            notNull = false
            return lhs
        }
    }

    fun nullable(): ColumnDefaultable.Nullable<T> =
        Nulled(this)
}