package io.koalaql.jdbc

import io.koalaql.Database
import io.koalaql.DeclareStrategy
import io.koalaql.Isolation
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.ddl.createTables as createTablesDdl
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import java.sql.Connection

class JdbcDatabase(
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings = JdbcTypeMappings()
): Database<JdbcConnection>() {
    override fun declareTablesUsing(declareBy: DeclareStrategy, tables: List<Table>) {
        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }

        val diff = SchemaDiff()

        when (declareBy) {
            DeclareStrategy.RegisterOnly -> return
            DeclareStrategy.CreateIfNotExists -> {
                tables.forEach {
                    diff.tables.created[it.relvarName] = it
                }
            }
            DeclareStrategy.Diff -> {
                tables.forEach {
                    diff.tables.created[it.relvarName] = it
                }
            }
        }

        transact { it.ddl(diff) }
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
            typeMappings
        )
    }

    override fun close() {
        provider.close()
    }
}