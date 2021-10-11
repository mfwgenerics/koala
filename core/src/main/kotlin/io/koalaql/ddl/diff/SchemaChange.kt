package io.koalaql.ddl.diff

import io.koalaql.ddl.Table

data class SchemaChange(
    val tables: Diff<String, Table, TableDiff> = Diff()
) {
    fun isEmpty(): Boolean = tables.isEmpty()
}