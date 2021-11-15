import io.koalaql.DeclareStrategy
import io.koalaql.jdbc.JdbcException
import kotlin.test.Test

class MysqlUnionableTests: UnionableTests() {
    override fun connect(db: String, declareBy: DeclareStrategy) = MysqlTestDatabase(db,
        declareBy = declareBy
    )

    @Test
    fun empty() { }

    override fun `except and intersect`() {
        /* mysql only supports UNION - no INTERSECT or EXCEPT */

        try {
            super.`except and intersect`()
            assert(false)
        } catch (ignored: JdbcException) {  }
    }

    @Test
    override fun `order by on values`() {
        try {
            /*
            bug in mysql prevents the values from actually being re-ordered so order by on values is a no-op
            still, the SQL should run so we expect an assertion failure rather than an exception
             */
            super.`order by on values`()
            assert(false)
        } catch (ex: AssertionError) {

        }
    }
}