import io.koalaql.dsl.*
import io.koalaql.expr.Reference
import io.koalaql.jdbc.GeneratedSqlException
import io.koalaql.jdbc.performWith
import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String) = MysqlTestDatabase(db)

    override fun `nulls first and last`() {
        /* mysql doesn't support NULLS FIRST and NULLS LAST */

        try {
            super.`nulls first and last`()
            assert(false)
        } catch (ignored: GeneratedSqlException) {  }
    }

    @Test
    fun likes() = withCxn { cxn, _ ->
        val result =
            select(
                (value("like") like "like") as_ name(),
                (value("like") like "lik%") as_ name(),
                (value("like") like "lik") as_ name()
            )
            .performWith(cxn)
            .map { row -> row.columns.map { row.getValue(it as Reference<Boolean>) } }
            .single()

        assertListEquals(listOf(true, true, false), result)
    }
}