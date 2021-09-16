import mfwgenerics.kotq.jdbc.JdbcDatabase
import mfwgenerics.kotq.jdbc.JdbcProvider
import mfwgenerics.kotq.postgres.PostgresDialect
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.Test

class TestPostgres: QueryTests() {
    override fun connect(db: String): JdbcDatabase {
        val outerCxn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "mysecretpassword")

        outerCxn.prepareStatement("CREATE DATABASE $db").execute()

        return JdbcDatabase(
            PostgresDialect(),
            object : JdbcProvider {
                override fun connect(): Connection =
                    DriverManager.getConnection("jdbc:postgresql://localhost:5432/$db", "postgres", "mysecretpassword")

                override fun close() {
                    outerCxn.prepareStatement("DROP DATABASE $db").execute()
                }
            }
        )
    }

    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}