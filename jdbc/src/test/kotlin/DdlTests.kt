import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.BaseColumnType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.Table.Companion.autoIncrement
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.ddl.diff.Alteration
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import mfwgenerics.kotq.dsl.keys
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
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

    private fun testExpectedTableDiff(
        cxn: ConnectionWithDialect,
        expected: TableDiff,
        table: Table
    ) {
        val differ = TableDiffer(cxn.jdbc.catalog, cxn.jdbc.metaData)

        val diff = differ
            .diffTable(table)

        expected.assertMatch(diff)

        cxn.ddl(SchemaDiff().apply {
            tables.altered[table.relvarName] = Alteration(
                table,
                diff
            )
        })

        val newDiff = differ.diffTable(table)
        TableDiff().assertMatch(newDiff)
    }

    @Test
    fun `empty diff`() = withCxn { cxn ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        testExpectedTableDiff(cxn, TableDiff(), CustomerTable)
    }

    @Test
    fun `change varchar lengths`() = withCxn { cxn ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        val differentTable = object : Table("Customer") {
            val id = column("id", INTEGER.autoIncrement())

            val firstName = column("firstName", VARCHAR(101))
            val lastName = column("lastName", VARCHAR(100))

            init {
                primaryKey(keys(id))
            }
        }

        testExpectedTableDiff(cxn,
            TableDiff()
                .apply {
                    columns.apply {
                        altered["firstName"] = Alteration(
                            value = differentTable.firstName,
                            alteration = ColumnDefinitionDiff(
                                type = BaseColumnType(VARCHAR(101))
                            )
                        )
                    }
                },
            differentTable
        )
    }
}