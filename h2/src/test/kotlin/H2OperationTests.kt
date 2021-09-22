import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDatabase
import org.junit.Test

class H2OperationTests: OperationTests() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}