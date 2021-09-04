import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.BaseColumnType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.Table.Companion.autoIncrement
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import mfwgenerics.kotq.dsl.keys
import mfwgenerics.kotq.jdbc.TableDiffer
import mfwgenerics.kotq.test.assertMatch
import kotlin.test.Test

abstract class DdlTests: ProvideTestDatabase {
    object CustomerTable: Table("Customer") {
        val id = column("id", INTEGER.autoIncrement())

        val firstName = column("firstName", VARCHAR(100))
        val lastName = column("lastName", VARCHAR(100))

        init {
            primaryKey(keys(id))
        }
    }

    @Test
    fun `empty diff`() = withCxn { cxn ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        val diff = TableDiffer(cxn.jdbc.catalog, cxn.jdbc.metaData).diffTable(
            CustomerTable
        )

        TableDiff().assertMatch(diff)
    }

    @Test
    fun `change varchar lengths`() = withCxn { cxn ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        val differentTable = Table("Customer").apply {
            val id = column("id", INTEGER.autoIncrement())

            val firstName = column("firstName", VARCHAR(101))
            val lastName = column("lastName", VARCHAR(100))

            primaryKey(keys(id))
        }

        val diff = TableDiffer(cxn.jdbc.catalog, cxn.jdbc.metaData).diffTable(
            differentTable
        )

        TableDiff()
            .apply {
                columns.apply {
                    altered["firstName"] = ColumnDefinitionDiff(
                        type = BaseColumnType(VARCHAR(101))
                    )
                }
            }
            .assertMatch(diff)

        println(diff)
    }
}