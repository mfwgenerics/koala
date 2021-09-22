import kotlin.test.Test

class TestPostgres: QueryTests() {
    override fun connect(db: String) = PgTestDatabase(db)

    override val requiresOnConflictKey get() = true

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }
}