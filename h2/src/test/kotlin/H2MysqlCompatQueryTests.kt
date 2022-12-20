import io.koalaql.DeclareStrategy
import io.koalaql.event.DataSourceEvent
import io.koalaql.h2.H2CompatibilityMode
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import kotlin.test.Test
import kotlin.test.assertFails

class H2MysqlCompatQueryTests: QueryTests() {
    override fun connect(
        db: String,
        declareBy: DeclareStrategy,
        events: DataSourceEvent
    ): JdbcDataSource =
        H2Database(db, mode = H2CompatibilityMode.MYSQL, declareBy = declareBy, events = events)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    override fun `deletion with cte`() {
        /* a CTE regression in H2 2.x.x breaks this */

        assertFails {
            super.`deletion with cte`()
        }
    }

    override fun `factorial recursive CTE`() {
        /* CTE support is experimental in H2 and doesn't work with the example */

        try {
            super.`factorial recursive CTE`()
            assert(false)
        } catch (ex: Exception) { }
    }
}
