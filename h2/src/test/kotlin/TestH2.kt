import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.jdbc.JdbcDatabase
import java.sql.DriverManager
import kotlin.test.Test

class TestH2: QueryTests() {
    override fun connect(db: String): JdbcDatabase = JdbcDatabase(
        H2Dialect(),
        { DriverManager.getConnection("jdbc:h2:mem:;") }
    )

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}