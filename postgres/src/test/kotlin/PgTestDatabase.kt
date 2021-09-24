import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.postgres.PostgresDialect
import java.sql.Connection
import java.sql.DriverManager

fun PgTestDatabase(db: String): JdbcDataSource {
    val outerCxn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "mysecretpassword")

    outerCxn.prepareStatement("CREATE DATABASE $db").execute()

    return JdbcDataSource(
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