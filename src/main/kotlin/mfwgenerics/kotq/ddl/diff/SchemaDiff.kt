package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.Table

data class SchemaDiff(
    val tables: Diff<String, Table, TableDiff> = Diff()
)