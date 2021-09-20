import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDatabase
import org.junit.Test

class H2DataTypesTests: DataTypesTest() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() { }
}