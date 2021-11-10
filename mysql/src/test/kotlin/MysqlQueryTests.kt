import io.koalaql.CreateIfNotExists
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.values
import io.koalaql.dsl.setTo
import io.koalaql.jdbc.JdbcException
import kotlin.test.Test

class MysqlQueryTests: QueryTests() {
    override fun connect(db: String) = MysqlTestDatabase(db, declareBy = CreateIfNotExists)

    @Test
    fun empty() {

    }

    override fun `nulls first and last`() {
        /* mysql doesn't support NULLS FIRST and NULLS LAST */

        try {
            super.`nulls first and last`()
            assert(false)
        } catch (ignored: JdbcException) {  }
    }

    @Test
    fun `row keyword omitted `() = withDb { db ->
        val sql = MappingsTable
            .insert(values(
                rowOf(
                    MappingsTable.number setTo NumberEnum.TWO,
                    MappingsTable.color setTo ColorEnum.BLUE,
                    MappingsTable.fruit setTo FruitEnum.BANANA
                )
            ))
            .generateSql(db)
            ?.parameterizedSql!!

        assert("VALUES (?, ?, ?)" in sql)
    }
}