package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.Database
import mfwgenerics.kotq.DeclareStrategy
import mfwgenerics.kotq.Isolation
import mfwgenerics.kotq.data.JdbcTypeMappings
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.ddl.createTables as createTablesDdl
import mfwgenerics.kotq.dialect.SqlDialect
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

    override fun connect(isolation: Isolation): JdbcConnection {
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