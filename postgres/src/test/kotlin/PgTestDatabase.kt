import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.postgres.PostgresDialect
import io.koalaql.test.retrying
import java.sql.Connection
import java.sql.DriverManager

fun PgTestDatabase(db: String, declareBy: DeclareStrategy): JdbcDataSource {
    val outerCxn = retrying {
        DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "mysecretpassword")
    }

    outerCxn.prepareStatement("CREATE DATABASE $db").execute()

    return JdbcDataSource(
        JdbcSchemaDetection.NotSupported,
        PostgresDialect(),
        object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:postgresql://localhost:5432/$db", "postgres", "mysecretpassword")

            override fun close() {
                outerCxn.prepareStatement("DROP DATABASE $db").execute()
            }
        },
        JdbcTypeMappings(),
        DeclareStrategy.CreateIfNotExists,
        DataSourceEvent.DISCARD
    )
}