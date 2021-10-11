package io.koalaql.jdbc

import io.koalaql.*
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import java.sql.Connection

class JdbcDataSource(
    val schema: JdbcSchemaDetection,
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings,
    val declareBy: DeclareStrategy
): SchemaDataSource {
    override fun detectChanges(tables: List<Table>): SchemaChange = provider.connect().use { jdbc ->
        jdbc.autoCommit = true

        val dbName = checkNotNull(jdbc.catalog?.takeIf { it.isNotBlank() })
            { "no database selected" }

        schema.detectChanges(dbName, jdbc.metaData, tables)
    }

    override fun changeSchema(changes: SchemaChange) {
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

    override fun declareTables(tables: List<Table>) {
        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }

        when (declareBy) {
            DeclareStrategy.RegisterOnly -> return
            DeclareStrategy.CreateIfNotExists -> {
                val diff = SchemaChange()

                tables.forEach {
                    diff.tables.created[it.relvarName] = it
                }

                changeSchema(diff)
            }
            DeclareStrategy.Change -> changeSchema(
                detectChanges(tables)
            )
            DeclareStrategy.Expect -> {
                val changes = detectChanges(tables)

                val ddl = dialect.ddl(changes)

                check (ddl.isEmpty() && changes.isEmpty()) {
                    "Schema differs from expectation. Differences:\n$changes\nDdl:\n$ddl"
                }
            }
        }
    }

    override fun connect(isolation: Isolation, events: ConnectionEventWriter): JdbcConnection {
        val jdbc = provider.connect()

        if (jdbc.autoCommit) jdbc.autoCommit = false

        val desiredIso = when (isolation) {
            Isolation.READ_UNCOMMITTED -> Connection.TRANSACTION_READ_COMMITTED
            Isolation.READ_COMMITTED -> Connection.TRANSACTION_READ_COMMITTED
            Isolation.REPEATABLE_READ -> Connection.TRANSACTION_REPEATABLE_READ
            Isolation.SERIALIZABLE -> Connection.TRANSACTION_SERIALIZABLE
        }

        if (jdbc.transactionIsolation != desiredIso) {
            jdbc.transactionIsolation = desiredIso
        }

        return JdbcConnection(
            jdbc,
            dialect,
            typeMappings,
            events
        )
    }

    fun close() {
        provider.close()
    }
}