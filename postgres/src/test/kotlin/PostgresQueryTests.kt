import kotlin.test.Test

class PostgresQueryTests: QueryTests(), PostgresTestProvider {
    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}