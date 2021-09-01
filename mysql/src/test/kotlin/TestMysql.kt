import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.dialect.mysql.MysqlDialect
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import java.sql.DriverManager
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestMysql: BaseTest() {
    override fun connect(): ConnectionWithDialect = ConnectionWithDialect(
        MysqlDialect(),
        DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb","root","my-secret-pw")
    )

    @BeforeTest
    fun clearDatabase() {
        val cxn = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root","my-secret-pw")

        cxn.prepareStatement("DROP DATABASE IF EXISTS testdb").execute()
        cxn.prepareStatement("CREATE DATABASE testdb").execute()

        cxn.close()
    }

    @Test
    fun `select version`() {
        val cxn = connect().jdbc

        val rs = cxn
            .prepareStatement("SELECT VERSION()")
            .executeQuery()

        while (rs.next()) {
            println(rs.getString(1))
        }

        cxn.close()
    }
}