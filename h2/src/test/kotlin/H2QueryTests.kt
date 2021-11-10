import io.koalaql.ddl.*
import io.koalaql.dsl.currentTimestamp
import io.koalaql.dsl.eq
import io.koalaql.h2.H2Database
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.table.VENUE_TYPE
import io.koalaql.test.table.VenueType
import kotlin.test.Test
import kotlin.test.assertEquals

class H2QueryTests: QueryTests() {
    override fun connect(db: String): JdbcDataSource = H2Database(db)

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    override fun `factorial recursive CTE`() {
        /* CTE support is experimental in H2 and doesn't work with the example */

        try {
            super.`factorial recursive CTE`()
            assert(false)
        } catch (ex: Exception) { }
    }

    override fun `on duplicate update with values`() {
        /* H2 does not support ON CONFLICT/ON DUPLICATE in its native dialect */

        try {
            super.`on duplicate update with values`()
            assert(false)
        } catch (ex: Exception) { }
    }

    @Test
    fun `generated sql`() = withDb { db ->
        val table = object : Table("Venue") {
            val created = column("create", TIMESTAMP.default(currentTimestamp()))

            val name = column("name", VARCHAR(128))
            val description = column("description", TEXT)

            val id = column("id", INTEGER.autoIncrement().primaryKey())

            val closedPermanently = column("closed", BOOLEAN.default(false))

            val type = column("type", VENUE_TYPE.default(VenueType.RESTAURANT))

            val rating = column("rating", FLOAT.nullable())
        }

        val expectedSql = """
            SELECT T0."create" "c0"
            , T0."name" "c1"
            , T0."description" "c2"
            , T0."id" "c3"
            , T0."closed" "c4"
            , T0."type" "c5"
            , T0."rating" "c6"
            FROM "Venue" T0
            WHERE T0."id" = ?
            ORDER BY T0."type" ASC, T0."name" DESC NULLS LAST
        """.trimIndent()

        val generated = table
            .where(table.id eq 10)
            .orderBy(table.type, table.name.desc().nullsLast())
            .selectAll()
            .generateSql(db)!!

        assertEquals(generated.parameterizedSql, expectedSql)
    }
}
