import mfwgenerics.kotq.jdbc.JdbcConnection
import mfwgenerics.kotq.jdbc.JdbcDatabase
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String): JdbcDatabase

    fun withCxn(block: (JdbcConnection) -> Unit) {
        val testDb = connect("db${SecureRandom().nextLong().absoluteValue}")

        try {
            testDb.transact {
                block(it)
            }
        } finally {
            testDb.close()
        }
    }
}