package io.koalaql.ddl

import io.koalaql.ddl.diff.SchemaChange

fun createTables(vararg tables: Table): SchemaChange {
    val result = SchemaChange()

    tables.forEach {
        result.tables.created[it.relvarName] = it
    }

    return result
}