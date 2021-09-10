import mfwgenerics.kotq.h2.H2Database
import mfwgenerics.kotq.jdbc.JdbcDatabase
import kotlin.test.Test

class H2QueryTests: QueryTests() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}