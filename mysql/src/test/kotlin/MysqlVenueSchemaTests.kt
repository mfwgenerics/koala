import io.koalaql.DeclareStrategy
import io.koalaql.test.service.OnConflictSupport
import org.junit.Test

class MysqlVenueSchemaTests: VenueSchemaTests() {
    override val onConflictSupport: OnConflictSupport get() = OnConflictSupport.ON_DUPLICATE

    override fun connect(db: String, declareBy: DeclareStrategy) = MysqlTestDatabase(db,
        declareBy = declareBy
    )

    @Test
    fun empty() { }
}