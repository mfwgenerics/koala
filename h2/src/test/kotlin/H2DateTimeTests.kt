import io.koalaql.h2.H2Database
import io.koalaql.h2.H2Dialect
import io.koalaql.h2.H2TypeMappings
import io.koalaql.jdbc.JdbcDatabase
import java.sql.DriverManager
import kotlin.test.Test

class H2DateTimeTests: DateTimeTests() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() { }
}