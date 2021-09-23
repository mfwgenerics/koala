package io.koalaql.jdbc

import io.koalaql.Database
import io.koalaql.DeclareStrategy
import io.koalaql.Isolation
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import java.sql.Connection

class JdbcDatabase(
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings = JdbcTypeMappings(),
    val declareBy: DeclareStrategy = DeclareStrategy.Diff
): Database<JdbcConnection>() {
    override fun declareTables(tables: List<Table>) {
        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }

        when (declareBy) {
            DeclareStrategy.RegisterOnly -> return
            DeclareStrategy.CreateIfNotExists -> {
                val diff = SchemaDiff()

                tables.forEach {
                    diff.tables.created[it.relvarName] = it
                }

                transact { it.ddl(diff) }
            }
            DeclareStrategy.Diff -> {
                provider.connect().use { jdbc ->
                    jdbc.autoCommit = true

                    val dbName = checkNotNull(jdbc.catalog?.takeIf { it.isNotBlank() })
                        { "no database selected" }

                    val differ = TableDiffer(
                        dbName,
                        jdbc.metaData
                    )

                    val diff = differ.declareTables(tables)

                    val connection = JdbcConnection(
                        jdbc,
                        dialect,
                        typeMappings,
                        ConnectionEventWriter.Discard
                    )

                    connection.ddl(diff)
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

    override fun close() {
        provider.close()
    }
}