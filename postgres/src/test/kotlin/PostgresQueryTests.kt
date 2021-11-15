import io.koalaql.DeclareStrategy
import kotlin.test.Test

class PostgresQueryTests: QueryTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) =
        PgTestDatabase(db, declareBy)

    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}