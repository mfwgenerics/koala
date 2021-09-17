package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.BuildsIntoColumnDef
import io.koalaql.ddl.built.BuiltColumnDef

interface ColumnIncrementable<T : Any>: ColumnNullable<T> {
    private class AutoIncrement<T : Any>(
        val lhs: ColumnIncrementable<T>
    ): ColumnNullable<T> {
        override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
            out.autoIncrement = true
            return lhs
        }
    }

    fun autoIncrement(): ColumnNullable<T> =
        AutoIncrement(this)
}