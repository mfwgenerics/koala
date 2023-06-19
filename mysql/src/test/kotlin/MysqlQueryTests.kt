import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
//import io.koalaql.dsl.*
import io.koalaql.expr.Expr
import io.koalaql.jdbc.JdbcException
import io.koalaql.mysql.generateMysqlSql
import java.time.Duration
import kotlin.test.Test

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
        val field = column("field", INTEGER.default(-1))
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
            .insert(values(1..10) {
                this[UpdateFrom.id] = it
                this[UpdateFrom.field] = it*it
            })
            .perform(cxn)

        val cte = cte()

        println(UpdateTo
            .leftJoin(cte, UpdateTo.id eq UpdateFrom.id)
            .update(
                UpdateTo.field setTo UpdateFrom.field
            )
            .with(cte as_ UpdateFrom
                .select(UpdateFrom.id, (UpdateFrom.field + 10) as_ UpdateFrom.field)
            )
            .generateMysqlSql())

        UpdateTo
            .leftJoin(cte, UpdateTo.id eq UpdateFrom.id)
            .update(
                UpdateTo.field setTo UpdateFrom.field
            )
            .with(cte as_ UpdateFrom
                .select(UpdateFrom.id, (UpdateFrom.field + 10) as_ UpdateFrom.field)
            )
            .perform(cxn)
    }
}