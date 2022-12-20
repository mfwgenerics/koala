import io.koalaql.test.service.OnConflictSupport
import org.junit.Test

class MysqlVenueSchemaTests: VenueSchemaTests(), MysqlTestProvider {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.ON_DUPLICATE

    @Test
    fun empty() { }
}