package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange

interface SchemaDataSource: DataSource {
    fun detectChanges(tables: List<Table>): SchemaChange
    fun changeSchema(changes: SchemaChange)

    fun detectAndApplyChanges(tables: List<Table>): SchemaChange {
        val result = detectChanges(tables)

        changeSchema(result)

        return result
    }
}