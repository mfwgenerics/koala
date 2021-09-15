import mfwgenerics.kotq.h2.H2Database
import mfwgenerics.kotq.jdbc.GeneratedSqlException
import mfwgenerics.kotq.jdbc.JdbcDatabase
import kotlin.test.Test

class H2QueryTests: QueryTests() {
    override fun connect(db: String): JdbcDatabase = H2Database(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    override fun `factorial recursive CTE`() {
        /* CTE support is experimental in H2 and doesn't work with the example */

        try {
            super.`factorial recursive CTE`()
        } catch (ex: GeneratedSqlException) { }
    }

    override fun `on duplicate update with values`() {
        /* H2 does not support ON CONFLICT/ON DUPLICATE in its native dialect */

        try {
            super.`on duplicate update with values`()
        } catch (ex: GeneratedSqlException) { }
    }
}
