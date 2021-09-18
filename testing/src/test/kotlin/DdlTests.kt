import io.koalaql.data.INTEGER
import io.koalaql.data.VARCHAR
import io.koalaql.ddl.BaseColumnType
import io.koalaql.ddl.Table
import io.koalaql.ddl.createTables
import io.koalaql.ddl.diff.ColumnDiff
import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.ddl.diff.TableDiff
import io.koalaql.dsl.keys
import io.koalaql.jdbc.JdbcConnection
import io.koalaql.jdbc.TableDiffer
import io.koalaql.test.assertMatch
import kotlin.test.Test

abstract class DdlTests: ProvideTestDatabase {
    object CustomerTable: Table("Customer") {
        val id = column("id", INTEGER.autoIncrement())

        val firstName = column("firstName", VARCHAR(100))
        val lastName = column("lastName", VARCHAR(100))

        init {
            primaryKey(keys(id))

            uniqueKey(keys(lastName, firstName))
        }
    }

    private fun testExpectedTableDiff(
        cxn: JdbcConnection,
        expected: TableDiff,
        table: Table
    ) {
        val differ = TableDiffer(cxn.jdbc.catalog, cxn.jdbc.metaData)

        val diff = differ
            .diffTable(table)

        expected.assertMatch(diff)

        cxn.ddl(SchemaDiff().apply {
            tables.altered[table.relvarName] = diff
        })

        val newDiff = differ.diffTable(table)
        TableDiff(table).assertMatch(newDiff)
    }

    @Test
    fun `empty diff`() = withCxn { cxn, logs ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        testExpectedTableDiff(cxn, TableDiff(CustomerTable), CustomerTable)
    }

    @Test
    fun `change varchar lengths and add unique key`() = withCxn { cxn, logs ->
        cxn.ddl(createTables(
            CustomerTable
        ))

        val differentTable = object : Table("Customer") {
            val id = column("id", INTEGER.autoIncrement())

            val firstName = column("firstName", VARCHAR(101))
            val lastName = column("lastName", VARCHAR(100))

            val namesKey = uniqueKey(keys(firstName, lastName))

            init {
                primaryKey(keys(id))
            }
        }

        testExpectedTableDiff(cxn,
            TableDiff(CustomerTable)
                .apply {
                    columns.apply {
                        altered["firstName"] = ColumnDiff(
                            newColumn = differentTable.firstName,
                            type = BaseColumnType(VARCHAR(101))
                        )
                    }

                    indexes.apply {
                        created["Customer_firstName_lastName_key"] = differentTable.namesKey.def
                        dropped.add("Customer_lastName_firstName_key")
                    }
                },
            differentTable
        )
    }
}