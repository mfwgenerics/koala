package io.koalaql.jdbc

import io.koalaql.*
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.Table
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.SqlDialect
import io.koalaql.event.ConnectionEventWriter
import java.sql.Connection

class JdbcDataSource(
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings = JdbcTypeMappings(),
    val declareBy: DeclareStrategy = DeclareStrategy.Diff
): DataSource {
    val existingSchema = SchemaSource(
        dialect,
        provider,
        typeMappings
    )

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

                existingSchema.applyChanges(diff)
            }
            DeclareStrategy.Diff -> existingSchema.applyChanges(
                existingSchema.detectChanges(tables)
            )
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