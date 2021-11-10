import io.koalaql.CreateIfNotExists
import io.koalaql.jdbc.JdbcException
import kotlin.test.Test

class MysqlUnionableTests: UnionableTests() {
    override fun connect(db: String) = MysqlTestDatabase(db, declareBy = CreateIfNotExists)

    @Test
    fun empty() { }

    override fun `except and intersect`() {
        /* mysql only supports UNION - no INTERSECT or EXCEPT */

        try {
            super.`except and intersect`()
            assert(false)
        } catch (ignored: JdbcException) {  }
    }
}