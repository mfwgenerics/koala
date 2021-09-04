package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.built.BuiltIndexDef

data class TableDiff(
    val columns: Diff<String, TableColumn<*>, ColumnDefinitionDiff> = Diff(),
    val indexes: Diff<String, BuiltIndexDef, Nothing> = Diff()
)