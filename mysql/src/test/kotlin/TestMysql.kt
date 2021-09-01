import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import java.sql.DriverManager
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestMysql: BaseTest() {
    override fun connect(): ConnectionWithDialect = ConnectionWithDialect(
        H2Dialect(),
        DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb","root","my-secret-pw")
    )

    @BeforeTest
    fun clearDatabase() {

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