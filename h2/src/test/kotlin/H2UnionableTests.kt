import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import org.junit.Test

class H2UnionableTests: UnionableTests() {
    override fun connect(db: String): JdbcDataSource = H2Database(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    override fun `union to values`() {
        try {
            /* H2 bug prevents this from working */
            super.`union to values`()
            assert(false)
        } catch (ex: Exception) { }
    }
}