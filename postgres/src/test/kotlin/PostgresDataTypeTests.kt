import io.koalaql.data.JdbcExtendedDataType
import io.koalaql.data.JdbcMappedType
import io.koalaql.ddl.*
import io.koalaql.dsl.*
import io.koalaql.expr.Expr
import io.koalaql.expr.ExtendedOperationType
import io.koalaql.expr.OperationFixity
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.test.assertContentEquals

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
            .onEach {
                println(it[TsVectorTable.text])
            }
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
}