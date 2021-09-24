import io.koalaql.KotqConnection
import io.koalaql.ddl.Table
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.logging.TextEventLogger
import io.koalaql.transact
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String): JdbcDataSource

    fun withDb(block: (JdbcDataSource) -> Unit) {
        val testDb = connect("db${SecureRandom().nextLong().absoluteValue}")

        try {
            block(testDb)
        } finally {
            testDb.close()
        }
    }

    fun withCxn(vararg tables: Table, block: (KotqConnection, List<String>) -> Unit) = withDb { db ->
        db.declareTables(*tables)

        val events = TextEventLogger("0")

        db.transact(events = events) { block(it, events.logs) }
    }
}