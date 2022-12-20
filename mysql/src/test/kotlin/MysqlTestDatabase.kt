import io.koalaql.DeclareStrategy
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.mysql.MysqlDataSource
import io.koalaql.test.retrying
import java.sql.Connection
import java.sql.DriverManager

fun MysqlTestDatabase(
    db: String,
    declareBy: DeclareStrategy,
    events: DataSourceEvent
): JdbcDataSource {
    val outerCxn = retrying {
        DriverManager.getConnection("jdbc:mysql://localhost:3306/","root","my-secret-pw")
    }

    outerCxn.prepareStatement("CREATE DATABASE $db").execute()

    val provider = object : JdbcProvider {
        override fun connect(): Connection =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/$db", "root", "my-secret-pw")

        override fun close() {
            outerCxn.prepareStatement("DROP DATABASE $db").execute()
        }
    }

    return MysqlDataSource(provider, declareBy, events)
}