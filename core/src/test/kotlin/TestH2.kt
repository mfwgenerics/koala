import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import java.sql.DriverManager

class TestH2: BaseTest() {
    override fun connect(): ConnectionWithDialect = ConnectionWithDialect(
        H2Dialect(),
        DriverManager.getConnection("jdbc:h2:mem:")
    )
}