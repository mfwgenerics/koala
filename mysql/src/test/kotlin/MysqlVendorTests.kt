import io.koalaql.jdbc.JdbcDatabase
import io.koalaql.test.service.OnConflictSupport
import org.junit.Test

class MysqlVendorTests: VenueSchemaTests() {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.ON_DUPLICATE

    override fun connect(db: String): JdbcDatabase = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}