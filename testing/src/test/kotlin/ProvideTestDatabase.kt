import io.koalaql.ddl.Table
import io.koalaql.jdbc.JdbcConnection
import io.koalaql.jdbc.JdbcDatabase
import io.koalaql.test.logging.TextEventLogger
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

    fun withCxn(vararg tables: Table, block: (JdbcConnection, List<String>) -> Unit) = withDb { db ->
        db.createTables(*tables)

        val events = TextEventLogger("0")

        db.transact(events = events) { block(it, events.logs) }
    }
}