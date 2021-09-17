import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDatabase
import kotlin.test.Test

class H2VendorTests: VenueSchemaTests() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() { }
}