import io.koalaql.data.JdbcExtendedDataType
import io.koalaql.data.JdbcMappedType
import io.koalaql.ddl.*
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.test.data.DataTypeValuesMap
import org.junit.Test
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet

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

    companion object {
        private val TSVECTOR = JdbcExtendedDataType("tsvector", object : JdbcMappedType<TsVector> {
            override fun writeJdbc(stmt: PreparedStatement, index: Int, value: TsVector) {
                stmt.setObject(index, PGobject().apply {
                    type = "tsvector"
                    this.value = value.text
                })
            }

            override fun readJdbc(rs: ResultSet, index: Int): TsVector? =
                rs.getString(index)?.let(::TsVector)
        })
    }

    private object TsVectorTable: Table("VectorTest") {
        val text = column("text", TEXT)
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
                    TsVectorTable.text setTo "$it",
                    TsVectorTable.tsvector setTo it?.let(::TsVector)
                )
            }))
            .perform(cxn)

        TsVectorTable
            .selectAll()
            .perform(cxn)
            .forEach {
                println(it[TsVectorTable.tsvector])
            }
    }
}