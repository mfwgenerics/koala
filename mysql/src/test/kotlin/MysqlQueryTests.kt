import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import io.koalaql.jdbc.JdbcException
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MysqlQueryTests: QueryTests(), MysqlTestProvider {
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

    private object UpdateTo: Table("UpdateTo") {
        val id = column("id", INTEGER)
        val field = column("field", INTEGER.nullable())
    }

    private object UpdateFrom: Table("UpdateFrom") {
        val id = column("id", INTEGER)
        val field = column("field", INTEGER)
    }

    @Test
    fun `joined update with cte`() = withCxn(
        UpdateTo,
        UpdateFrom
    ) { cxn ->
        UpdateFrom
            .insert(values(0..2) {
                this[UpdateFrom.id] = it
                this[UpdateFrom.field] = it*it
            })
            .perform(cxn)

        UpdateTo
            .insert(values(0..5) {
                this[UpdateTo.id] = it
            })
            .perform(cxn)

        val cte = cte()

        UpdateTo
            .leftJoin(cte, UpdateTo.id eq UpdateFrom.id)
            .update(UpdateTo.field setTo coalesce(UpdateFrom.field, 0))
            .with(cte as_ UpdateFrom
                .select(UpdateFrom.id, (UpdateFrom.field + 10) as_ UpdateFrom.field)
            )
            .perform(cxn)

        assertContentEquals(
            listOf(
                Pair(0, 10),
                Pair(1, 11),
                Pair(2, 14),
                Pair(3, 0),
                Pair(4, 0),
                Pair(5, 0),
            ),
            UpdateTo
                .select(UpdateTo.id, UpdateTo.field)
                .perform(cxn)
                .map { Pair(it.first(), it.second()) }
                .toList()
        )
    }
}