package io.koalaql.ddl.diff

import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.built.BuiltIndexDef

data class TableDiff(
    val newTable: Table,

    val columns: Diff<String, TableColumn<*>, ColumnDiff> = Diff(),
    val indexes: Diff<String, BuiltIndexDef, BuiltIndexDef> = Diff()
) {
    fun isEmpty() = columns.isEmpty()
        && indexes.isEmpty()
}