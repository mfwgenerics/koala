import org.junit.Test

class PostgresReturningTests: ReturningTests(), PostgresTestProvider {
    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}