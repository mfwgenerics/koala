import io.koalaql.ddl.*
import io.koalaql.dsl.*
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test

class PostgresDataTypeTests: DataTypesTest(), PostgresTestProvider {
    override fun compatibilityAdjustment(values: DataTypeValuesMap) {
        /* these are not supported by postgres */
        values.remove(TINYINT)
        values.remove(TINYINT.UNSIGNED)
        values.remove(SMALLINT.UNSIGNED)
        values.remove(INTEGER.UNSIGNED)
        values.remove(BIGINT.UNSIGNED)
        values.remove(VARBINARY(200))
    }

    object JsonBTable: Table("JsonBTable") {
        val jsonb = column("jsonb", JSONB)
    }

    @Test
    fun empty() {
        /* prevents test runner from skipping the base class tests */
    }

    @Test
    fun `jsonb works`() = withCxn(JsonBTable) { cxn ->
        val label = label<JsonData>()

        JsonBTable
            .insert(rowOf(JsonBTable.jsonb setTo JsonData("""{"items":[{"test":{}}]}""")))
            .perform(cxn)

        val result = JsonBTable
            .select(JsonBTable.jsonb, cast(JsonBTable.jsonb, JSONB) as_ label)
            .perform(cxn)
            .first()

        println("${result.first()}, ${result.second()}")
    }
}