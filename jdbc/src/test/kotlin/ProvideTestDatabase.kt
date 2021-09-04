import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.test.TestDatabase
import java.security.SecureRandom
import kotlin.math.absoluteValue

interface ProvideTestDatabase {
    fun connect(db: String): TestDatabase

    fun withCxn(block: (ConnectionWithDialect) -> Unit) {
        val testDb = connect("db${SecureRandom().nextLong().absoluteValue}")

        try {
            block(testDb.cxn)
        } finally {
            testDb.drop()
        }
    }
}