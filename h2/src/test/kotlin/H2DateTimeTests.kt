import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import kotlin.test.Test

class H2DateTimeTests: DateTimeTests() {
    override fun connect(db: String): JdbcDataSource = H2Database(db)

    @Test
    fun empty() { }
}