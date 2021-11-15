import io.koalaql.DeclareStrategy
import org.junit.Test

class PostgresOperationTests: OperationTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) =
        PgTestDatabase(db, declareBy)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}