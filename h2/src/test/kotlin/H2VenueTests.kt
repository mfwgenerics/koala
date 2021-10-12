import io.koalaql.h2.H2CompatibilityMode
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.service.OnConflictSupport
import kotlin.test.Test

class H2VenueTests: VenueSchemaTests() {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.ON_DUPLICATE

    override fun connect(db: String): JdbcDataSource = H2Database(db, H2CompatibilityMode.MYSQL)

    @Test
    fun empty() { }
}