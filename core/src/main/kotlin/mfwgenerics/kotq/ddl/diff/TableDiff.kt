package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.BuiltIndexDef

data class TableDiff(
    val newTable: Table,

    val columns: Diff<String, TableColumn<*>, ColumnDiff> = Diff(),
    val indexes: Diff<String, BuiltIndexDef, BuiltIndexDef> = Diff()
)