import io.koalaql.jdbc.GeneratedSqlException
import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    @Test
    fun empty() {

    }

    override fun `nulls first and last`() {
        /* mysql doesn't support NULLS FIRST and NULLS LAST */

        try {
            super.`nulls first and last`()
            assert(false)
        } catch (ignored: GeneratedSqlException) {  }
    }
}