import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.h2.H2TypeMappings
import mfwgenerics.kotq.jdbc.JdbcDatabase
import java.sql.DriverManager

class H2DateTimeTests: DateTimeTests() {
    override fun connect(db: String): JdbcDatabase = JdbcDatabase(
        H2Dialect(),
        { DriverManager.getConnection("jdbc:h2:mem:;") },
        H2TypeMappings()
    )
}