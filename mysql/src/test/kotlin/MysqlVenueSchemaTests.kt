import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.service.OnConflictSupport
import org.junit.Test

class MysqlVenueSchemaTests: VenueSchemaTests() {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.ON_DUPLICATE

    override fun connect(db: String): JdbcDataSource = MysqlTestDatabase(db)

    @Test
    fun empty() { }
}