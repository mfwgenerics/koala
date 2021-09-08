package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.Database
import mfwgenerics.kotq.DeclareStrategy
import mfwgenerics.kotq.Isolation
import mfwgenerics.kotq.data.JdbcTypeMappings
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dialect.SqlDialect
import java.sql.Connection

class JdbcDatabase(
    val dialect: SqlDialect,
    val provider: JdbcProvider,
    val typeMappings: JdbcTypeMappings = JdbcTypeMappings(),
    val declareBy: DeclareStrategy = DeclareStrategy.RegisterOnly
): Database<JdbcConnection>() {
    override fun declare(vararg tables: Table) {
        tables.forEach { table ->
            table.columns.forEach {
                typeMappings.register(it.builtDef.columnType)
            }
        }

        // TOOD use declareBy
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