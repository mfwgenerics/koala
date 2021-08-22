package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.ddl.diff.SchemaDiff

fun createTables(vararg tables: Table): SchemaDiff {
    val result = SchemaDiff()

    tables.forEach {
        result.tables.created[it.relvarName] = it
    }

    return result
}