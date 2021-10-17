import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.mysql.MysqlDataSource
import io.koalaql.test.retrying
import java.sql.Connection
import java.sql.DriverManager

fun MysqlTestDatabase(db: String): JdbcDataSource {
    val outerCxn = retrying {
        println(System.currentTimeMillis())

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

    return MysqlDataSource(provider)
}