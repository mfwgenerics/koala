import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.postgres.PostgresDialect
import mfwgenerics.kotq.test.TestDatabase
import java.sql.DriverManager
import kotlin.test.Test

class TestPostgres: QueryTests() {
    override fun connect(db: String): TestDatabase {
        val outerCxn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "mysecretpassword")

        outerCxn.prepareStatement("CREATE DATABASE $db").execute()

        return object : TestDatabase {
            override val cxn: ConnectionWithDialect = ConnectionWithDialect(
                PostgresDialect(),
                DriverManager.getConnection("jdbc:postgresql://localhost:5432/$db", "postgres", "mysecretpassword")
            )

            override fun drop() {
                cxn.jdbc.close()
                outerCxn.prepareStatement("DROP DATABASE $db").execute()
            }
        }
    }

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}