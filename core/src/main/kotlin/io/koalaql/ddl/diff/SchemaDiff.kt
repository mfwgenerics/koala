package io.koalaql.ddl.diff

import io.koalaql.ddl.Table

data class SchemaDiff(
    val tables: Diff<String, Table, TableDiff> = Diff()
)