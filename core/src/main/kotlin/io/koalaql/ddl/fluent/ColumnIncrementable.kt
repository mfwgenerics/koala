package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.built.ColumnDefBuilder

interface ColumnIncrementable<T : Any>: ColumnNullable<T> {
    private class AutoIncrement<T : Any>(
        val lhs: ColumnIncrementable<T>
    ): ColumnNullable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder? {
            autoIncrement = true
            return lhs
        }
    }

    fun autoIncrement(): ColumnNullable<T> =
        AutoIncrement(this)
}