import org.junit.Test

class PostgresOperationTests: OperationTests(), PostgresTestProvider {
    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}