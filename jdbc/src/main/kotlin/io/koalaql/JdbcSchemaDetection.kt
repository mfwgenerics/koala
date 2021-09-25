package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import java.sql.DatabaseMetaData

interface JdbcSchemaDetection {
    fun detectChanges(
        dbName: String,
        metadata: DatabaseMetaData,
        tables: List<Table>
    ): SchemaChange

    object NotSupported: JdbcSchemaDetection {
        override fun detectChanges(dbName: String, metadata: DatabaseMetaData, tables: List<Table>): SchemaChange {
            error("JDBC schema detection is not supported for this database")
        }
    }
}