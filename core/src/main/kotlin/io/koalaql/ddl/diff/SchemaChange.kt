package io.koalaql.ddl.diff

import io.koalaql.ddl.Table
import io.koalaql.ddl.TableName

data class SchemaChange(
    val tables: Diff<TableName, Table, TableDiff> = Diff()
) {
    fun isEmpty(): Boolean = tables.isEmpty()
}