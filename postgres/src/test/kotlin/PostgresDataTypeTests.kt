import io.koalaql.ddl.*
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
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

    private data class TsVector(
        val text: String
    )

    private object TsVectorTable: Table("VectorTest") {
        val text = column("text", TEXT)
        val tsvector = column("vector", RAW<TsVector>("tsvector"))
    }

    @Test
    fun `user defined tsvector`() = withCxn(TsVectorTable) { cxn ->
        val texts = listOf(
            "Koala is a Kotlin JVM library for building and executing SQL.",
            "It is designed to be a more powerful and complete alternative to ...",
            "... the SQL DSL layer in ORMs like Ktorm and Exposed."
        )

        TsVectorTable
            .insert(values(texts.map {
                rowOf(
                    TsVectorTable.text setTo it,
                    TsVectorTable.tsvector setTo TsVector(it)
                )
            }))
            .perform(cxn)
    }
}