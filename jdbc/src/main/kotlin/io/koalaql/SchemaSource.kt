package io.koalaql

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.jdbc.JdbcConnection
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.jdbc.TableDiffer

class SchemaSource(
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings
) {
    fun detectChanges(tables: List<Table>): SchemaChange = provider.connect().use { jdbc ->
        jdbc.autoCommit = true

        val dbName = checkNotNull(jdbc.catalog?.takeIf { it.isNotBlank() })
            { "no database selected" }

        val differ = TableDiffer(
            dbName,
            jdbc.metaData
        )

        differ.declareTables(tables)
    }

    fun applyChanges(changes: SchemaChange) {
        val batches = dialect.ddl(changes)

        provider.connect().use { jdbc ->
            jdbc.autoCommit = true

            val connection = JdbcConnection(
                jdbc,
                dialect,
                typeMappings,
                ConnectionEventWriter.Discard
            )

            batches.forEach {
                connection.prepareAndThen(it) {
                    use {
                        execute()
                    }
                }
            }
        }
    }

    fun detectAndApplyChanges(tables: List<Table>): SchemaChange {
        val result = detectChanges(tables)

        applyChanges(result)

        return result
    }
}