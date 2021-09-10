import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.jdbc.JdbcConnection
import mfwgenerics.kotq.jdbc.JdbcDatabase
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String): JdbcDatabase

    fun withDb(block: (JdbcDatabase) -> Unit) {
        val testDb = connect("db${SecureRandom().nextLong().absoluteValue}")

        try {
            block(testDb)
        } finally {
            testDb.close()
        }
    }

    fun withCxn(vararg tables: Table, block: (JdbcConnection) -> Unit) = withDb { db ->
        db.createTables(*tables)

        db.transact { block(it) }
    }
}