package io.koalaql.ddl

import io.koalaql.ddl.diff.SchemaDiff

fun createTables(vararg tables: Table): SchemaDiff {
    val result = SchemaDiff()

    tables.forEach {
        result.tables.created[it.relvarName] = it
    }

    return result
}