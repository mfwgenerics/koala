package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.ColumnDefBuilder
import io.koalaql.ddl.built.BuiltColumnDef

interface ColumnNullable<T : Any>: ColumnDefaultable<T> {
    private class Nulled<T : Any>(
        val lhs: ColumnNullable<T>
    ): ColumnDefaultable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
            notNull = false
            return lhs
        }
    }

    fun nullable(): ColumnDefaultable<T> =
        Nulled(this)
}