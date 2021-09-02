import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.test.TestDatabase
import java.sql.DriverManager

class TestH2: BaseTest() {
    override fun connect(db: String): TestDatabase = object : TestDatabase {
        override val cxn: ConnectionWithDialect = ConnectionWithDialect(
            H2Dialect(),
            DriverManager.getConnection("jdbc:h2:mem:")
        )

        override fun drop() {
            cxn.jdbc.close()
        }
    }
}