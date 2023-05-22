import io.koalaql.ReconciledChanges
import io.koalaql.ReconciledDdl
import io.koalaql.data.JdbcExtendedDataType
import io.koalaql.data.JdbcMappedType
import io.koalaql.ddl.*
import io.koalaql.dsl.*
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.DataSourceChangeEvent
import io.koalaql.event.DataSourceEvent
import io.koalaql.expr.Expr
import io.koalaql.expr.ExtendedOperationType
import io.koalaql.expr.OperationFixity
import io.koalaql.query.Alias
import io.koalaql.sql.CompiledSql
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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

    private object TsQuery /* used at type-level only */

    private data class TsVector(
        val text: String /* read/display only */
    )

    private infix fun Expr<TsVector>.`@@`(other: Expr<TsQuery>): Expr<Boolean> =
        ExtendedOperationType("@@", OperationFixity.INFIX)(this, other)

    private fun toTsQuery(query: String): Expr<TsQuery> =
        ExtendedOperationType("to_tsquery", OperationFixity.APPLY)(value(query))

    private fun toTsVector(query: String): Expr<TsVector> =
        ExtendedOperationType("to_tsvector", OperationFixity.APPLY)(value(query))

    private object TsVectorTable: Table("VectorTest") {
        private val TSVECTOR = JdbcExtendedDataType("tsvector", object : JdbcMappedType<TsVector> {
            override fun writeJdbc(stmt: PreparedStatement, index: Int, value: TsVector) {
                error("TsVector can not be inserted directly. use toTsVector")
            }

            override fun readJdbc(rs: ResultSet, index: Int): TsVector? =
                rs.getString(index)?.let(::TsVector)
        })

        val text = column("text", TEXT.nullable())
        val tsvector = column("vector", TSVECTOR.nullable())
    }

    @Test
    fun `user defined tsvector`() = withCxn(TsVectorTable) { cxn ->
        val texts = listOf(
            "Koala is a Kotlin JVM library for building and executing SQL.",
            "It is designed to be a more powerful and complete alternative to ...",
            null,
            "... the SQL DSL layer in ORMs like Ktorm and Exposed."
        )

        TsVectorTable
            .insert(values(texts.map {
                rowOf(
                    TsVectorTable.text setTo it,
                    TsVectorTable.tsvector setTo it?.let(::toTsVector)
                )
            }))
            .perform(cxn)

        val matchedPower = label<Boolean>()
        val matchedLibrary = label<Boolean>()

        val results = TsVectorTable
            .orderBy(TsVectorTable.text.nullsFirst())
            .select(
                TsVectorTable.text,
                (TsVectorTable.tsvector `@@` toTsQuery("power")) as_ matchedPower,
                (TsVectorTable.tsvector `@@` toTsQuery("library")) as_ matchedLibrary
            )
            .perform(cxn)
            .map { Pair(it[matchedPower], it[matchedLibrary]) }
            .toList()

        assertContentEquals(
            listOf(
                Pair(null, null),
                Pair(true, false),
                Pair(false, true),
                Pair(false, false)
            ),
            results
        )
    }

    object JsonBTable: Table("JsonBTable") {
        val jsont = column("jsont", JSON)
        val jsonb = column("jsonb", JSONB)
    }

    @Test
    fun `jsonb works`() {
        val ddls = arrayListOf<CompiledSql>()

        withDb(
            events = object : DataSourceEvent {
                override fun changes(changes: ReconciledChanges, ddl: ReconciledDdl) = object : DataSourceChangeEvent {
                    override fun applied(ddl: List<CompiledSql>) {
                        ddls.addAll(ddl)
                    }
                }

                override fun connect() = ConnectionEventWriter.Discard
            }
        ) { db ->
            ddls.clear()

            db.declareTables(JsonBTable)

            val createTable = ddls.single().parameterizedSql

            assertEquals(
                """
                CREATE TABLE IF NOT EXISTS "JsonBTable"(
                "jsont" JSON NOT NULL,
                "jsonb" JSONB NOT NULL
                )
                """.trimIndent(),
                createTable
            )

            val castJson = label<JsonData>()
            val castJsonB = label<JsonData>()

            /* the unusual whitespace here is being used to functionally test JSON vs JSONB storage */
            val jsonText = """{"items"  :  [{"test":{}}]}"""

            JsonBTable
                .insert(
                    rowOf(
                        JsonBTable.jsont setTo JsonData(jsonText),
                        JsonBTable.jsonb setTo JsonData(jsonText),
                    )
                )
                .perform(db)

            val query = JsonBTable
                .select(
                    JsonBTable.jsont, JsonBTable.jsonb,
                    cast(JsonBTable.jsonb, JSON) as_ castJson,
                    cast(JsonBTable.jsont, JSONB) as_ castJsonB
                )

            val sql = query
                .generateSql(db)
                ?.parameterizedSql

            /*
            Currently, the best way to check the correct use of JSON
            vs JSONB column type is asserting against generated SQL
            */
            assertEquals(
                """
                SELECT T0."jsont" c0
                , T0."jsonb" c1
                , CAST(T0."jsonb" AS JSON) c2
                , CAST(T0."jsont" AS JSONB) c3
                FROM "JsonBTable" T0
                """.trimIndent(),
                sql
            )

            val row = query
                .perform(db)
                .single()

            assertEquals(jsonText, row[JsonBTable.jsont].asString)
            assertEquals("""{"items": [{"test": {}}]}""", row[JsonBTable.jsonb].asString)
            assertEquals("""{"items": [{"test": {}}]}""", row.getValue(castJson).asString)
            assertEquals("""{"items": [{"test": {}}]}""", row.getValue(castJsonB).asString)
        }
    }
}