import io.koalaql.jdbc.JdbcDatabase
import io.koalaql.jdbc.JdbcProvider
import io.koalaql.mysql.MysqlDialect
import io.koalaql.mysql.MysqlTypeMappings
import java.sql.Connection
import java.sql.DriverManager

fun MysqlTestDatabase(db: String): JdbcDatabase {
    val outerCxn = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root","my-secret-pw")

    outerCxn.prepareStatement("CREATE DATABASE $db").execute()

    return JdbcDatabase(
        MysqlDialect(),
        object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:mysql://localhost:3306/$db", "root", "my-secret-pw")

            override fun close() {
                outerCxn.prepareStatement("DROP DATABASE $db").execute()
            }
        },
        MysqlTypeMappings()
    )
}