import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.service.OnConflictSupport
import kotlin.test.Test

class H2VenueTests: VenueSchemaTests() {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.NONE

    override fun connect(db: String): JdbcDataSource = H2Database(db)

    @Test
    fun empty() { }
}