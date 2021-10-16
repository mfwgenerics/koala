package io.koalaql.ddl.built

interface ColumnDefBuilder {
    fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder?
}