import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.mysql.MysqlDialect
import mfwgenerics.kotq.test.TestDatabase
import java.sql.DriverManager

class MysqlTestDatabase(
    val db: String
): TestDatabase {
    val outerCxn = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root","my-secret-pw")

    init {
        outerCxn.prepareStatement("CREATE DATABASE $db").execute()
    }

    override val cxn: ConnectionWithDialect = ConnectionWithDialect(
        MysqlDialect(),
        DriverManager.getConnection("jdbc:mysql://localhost:3306/$db", "root", "my-secret-pw")
    )

    override fun drop() {
        cxn.jdbc.close()
        outerCxn.prepareStatement("DROP DATABASE $db").execute()
    }
}