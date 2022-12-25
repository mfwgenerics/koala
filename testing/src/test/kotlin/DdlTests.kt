import io.koalaql.DeclareStrategy
import io.koalaql.ddl.*
import io.koalaql.ddl.Table.Companion.default
import io.koalaql.ddl.Table.Companion.using
import io.koalaql.ddl.diff.ColumnDiff
import io.koalaql.ddl.diff.Diff
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.ddl.diff.TableDiff
import io.koalaql.dsl.keys
import io.koalaql.dsl.value
import io.koalaql.expr.Literal
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.test.assertMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
        expected: SchemaChange,
        table: Table
    ) {
        val diff = db.detectChanges(listOf(table))

        expected.assertMatch(diff)

        db.changeSchema(diff)

        val changes = db.detectChanges(listOf(table))

        assert(changes.isEmpty()) { "unexpected changes: $changes" }
    }

    @Test
    fun `empty diff`() = withDb { db ->
        db.changeSchema(createTables(
            CustomerTable
        ))

        testExpectedTableDiff(db, SchemaChange(), CustomerTable)
    }

    @Test
    fun `change varchar lengths and add unique key`() = withDb { db ->
        db.changeSchema(createTables(
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
            SchemaChange(
                tables = Diff(
                    altered = mutableMapOf(CustomerTable.tableName to
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
                            }
                    )
                )
            ),
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
            TIMESTAMP(0),
            BIGINT,
            BOOLEAN,
            DATE,
            DATETIME,
            TIMESTAMP(6),
            DOUBLE,
            FLOAT,
            TIMESTAMP,
            INTEGER,
            SMALLINT,
            TEXT,
            TIME(0),
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
            VARBINARY(200)
        )

        val filteredColumnTypes = columnTypes
            .filter { supportedColumnTypes(it) }

        val cases = filteredColumnTypes
            .mapIndexed { ix, it ->
                listOf(it, filteredColumnTypes[(ix + 1) % filteredColumnTypes.size], filteredColumnTypes[(ix + 3) % filteredColumnTypes.size])
            }
            .withIndex()
            .associateBy({ "${it.index}" }) { it.value }

        class TestTable(ix: Int): Table("Test") {
            init {
                cases.forEach { (name, cases) ->
                    @Suppress("unchecked_cast")
                    val dataType = cases[ix] as UnmappedDataType<Any>

                    column(name, dataType.using {
                        /* to make this work with postgres we need to include a `using null` to avoid AoT cast/convert error */
                        Literal(dataType.type, null)
                    })
                }
            }
        }

        /* pivot columns into table definitions */
        val diffs = (0..2)
            .map { ix -> TestTable(ix) }
            .flatMap { listOf(it, it) }
            .map {
                db.detectAndApplyChanges(listOf(it))
            }

        val createdAlteredDropped = diffs.map { diff ->
            assertEquals(0, diff.tables.dropped.size)

            Pair(
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

        val expected = listOf(
            Pair(1, 0),
            Pair(0, 0),
            Pair(0, cases.size),
            Pair(0, 0),
            Pair(0, cases.size),
            Pair(0, 0)
        )

        expected.forEachIndexed { ix, pair ->
            assertEquals(pair, createdAlteredDropped[ix], "${diffs[ix]}")
        }
    }

    @Test
    fun `drop columns rejected`() = withDb(
        declareBy = DeclareStrategy.NO_DROP
    ) { db ->
        val table1 = object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116).default("test"))
            val column1 = column("column1", INTEGER)
        }

        assert(!db.detectChanges(listOf(table1)).isEmpty())

        db.declareTables(object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116).default("test"))
            val column1 = column("column1", INTEGER)
        })

        assert(db.detectChanges(listOf(table1)).isEmpty())

        val table2 = object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116))
            val column1 = column("column1", INTEGER)
        }

        assert(!db.detectChanges(listOf(table2)).isEmpty())

        db.declareTables(table2)

        assert(db.detectChanges(listOf(table2)).isEmpty())

        assertFails {
            db.declareTables(object : Table("TestTable") {
                val column0 = column("column0", VARCHAR(116))
            })
        }
    }


    @Test
    fun `modify columns rejected`() = withDb(
        declareBy = DeclareStrategy.CREATE_ONLY
    ) { db ->
        val table1 = object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116).default("test"))
            val column1 = column("column1", INTEGER)
        }

        assert(!db.detectChanges(listOf(table1)).isEmpty())

        db.declareTables(object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116).default("test"))
            val column1 = column("column1", INTEGER)
        })

        assert(db.detectChanges(listOf(table1)).isEmpty())

        val table2 = object : Table("TestTable") {
            val column0 = column("column0", VARCHAR(116))
            val column1 = column("column1", INTEGER)
        }

        assert(!db.detectChanges(listOf(table2)).isEmpty())

        assertFails {
            db.declareTables(table2)
        }
    }
}