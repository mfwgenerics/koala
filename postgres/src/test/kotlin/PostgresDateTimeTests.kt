import org.junit.Test

class PostgresDateTimeTests: DateTimeTests(), PostgresTestProvider {
    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}