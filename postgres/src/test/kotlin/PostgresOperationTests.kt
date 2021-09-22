import org.junit.Test

class PostgresOperationTests: OperationTests() {
    override fun connect(db: String) = PgTestDatabase(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}