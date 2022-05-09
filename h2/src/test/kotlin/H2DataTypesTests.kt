import io.koalaql.DeclareStrategy
import io.koalaql.ddl.*
import io.koalaql.dsl.*
import io.koalaql.expr.Label
import io.koalaql.h2.H2Database
import io.koalaql.identifier.Unnamed
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test

class H2DataTypesTests: DataTypesTest() {
    override fun connect(db: String, declareBy: DeclareStrategy): JdbcDataSource =
        H2Database(db, declareBy = declareBy)

    @Test
    fun empty() { }

    override fun compatibilityAdjustment(values: DataTypeValuesMap) {
        /* H2 doesn't have UNSIGNED support - aliases the syntax to underlying int type */
        values.remove(TINYINT.UNSIGNED)
        values.remove(SMALLINT.UNSIGNED)
        values.remove(INTEGER.UNSIGNED)
        values.remove(BIGINT.UNSIGNED)
    }

    object JsonTable: Table("JsonTable") {
        val json = column("json", JSON)
    }

    @Test
    fun `json works`() = withCxn(JsonTable) { cxn ->
        val label = label<JsonData>()
        val casted = cast(label, JSON)

        val rows = values(listOf(JsonData(""""""""))) { this[label] = it }
            .subquery()
            .select(casted as_ label)
            .also { println(it.generateSql(cxn)) }
            .perform(cxn)
            .map { it.getValue(label) }
            .toList()

        JsonTable
            .insert(rowOf(JsonTable.json setTo JsonData("{}")))
            .perform(cxn)

        val result = JsonTable
            .select(JsonTable.json, cast(JsonTable.json, JSON) as_ label)
            .perform(cxn)
            .first()

        println("${result.first()}, ${result.second()}")
    }
}