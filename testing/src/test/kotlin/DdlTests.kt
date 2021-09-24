import io.koalaql.data.*
import io.koalaql.ddl.BaseColumnType
import io.koalaql.ddl.Table
import io.koalaql.ddl.createTables
import io.koalaql.ddl.diff.ColumnDiff
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.ddl.diff.TableDiff
import io.koalaql.dsl.keys
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.assertMatch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

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
        db: JdbcDataSource,
        expected: TableDiff,
        table: Table
    ) {
        val diff = db.existingSchema.detectChanges(listOf(table))
            .tables.altered.values.single()

        expected.assertMatch(diff)

        db.existingSchema.applyChanges(SchemaChange().apply {
            tables.altered[table.relvarName] = diff
        })

        val newDiff = db.existingSchema.detectChanges(listOf(table))
            .tables.altered.values.single()

        TableDiff(table).assertMatch(newDiff)
    }

    @Test
    fun `empty diff`() = withDb { db ->
        db.existingSchema.applyChanges(createTables(
            CustomerTable
        ))

        testExpectedTableDiff(db, TableDiff(CustomerTable), CustomerTable)
    }

    @Test
    fun `change varchar lengths and add unique key`() = withDb { db ->
        db.existingSchema.applyChanges(createTables(
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

        testExpectedTableDiff(db,
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

    abstract fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean

    @Test
    fun `table with all databases gets type shuffled`() = withDb { db ->
        val columnTypes = listOf(
            DECIMAL(4, 2),
            DECIMAL(5, 4),
            DECIMAL(8, 4),
            DECIMAL(7, 1),
            BIGINT,
            BOOLEAN,
            DATE,
            DATETIME,
            DOUBLE,
            FLOAT,
            INSTANT,
            INTEGER,
            SMALLINT,
            TEXT,
            TIME(5),
            TINYINT,
            TINYINT.UNSIGNED,
            SMALLINT.UNSIGNED,
            INTEGER.UNSIGNED,
            BIGINT.UNSIGNED,
            VARCHAR(100),
            VARCHAR(150),
            VARCHAR(200),
            VARCHAR(250),
            VARBINARY(200),
            RAW<BigDecimal>("DECIMAL(5, 4)")
        )

        val cases = columnTypes
            .filter { supportedColumnTypes(it) }
            .mapIndexed { ix, it ->
                listOf(it, columnTypes[(ix + 1) % columnTypes.size], columnTypes[(ix + 3) % columnTypes.size])
            }
            .withIndex()
            .associateBy({ "${it.index}" }) { it.value }

        class TestTable(ix: Int): Table("Test") {
            init {
                cases.forEach { (name, cases) ->
                    column(name, cases[ix])
                }
            }
        }

        val schema = db.existingSchema

        /* pivot columns into table definitions */
        val diffs = (0..2)
            .map { ix -> TestTable(ix) }
            .flatMap { listOf(it, it) }
            .map {
                schema.detectAndApplyChanges(listOf(it))
            }

        val createdAlteredDropped = diffs.map { diff ->
            assertEquals(0, diff.tables.dropped.size)

            listOf(
                diff.tables.created.size,
                diff.tables.altered.values.sumOf {
                    assertEquals(0, it.columns.created.size)
                    assertEquals(0, it.columns.dropped.size)
                    assertEquals(0, it.indexes.created.size)
                    assertEquals(0, it.indexes.altered.size)
                    assertEquals(0, it.indexes.dropped.size)

                    it.columns.altered.size
                }
            )
        }

        createdAlteredDropped.forEach {
            println(it)
        }

        TODO()
    }
}