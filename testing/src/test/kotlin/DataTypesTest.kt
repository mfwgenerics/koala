import io.koalaql.IdentifierName
import io.koalaql.DataConnection
import io.koalaql.ddl.*
import io.koalaql.dsl.*
import io.koalaql.expr.Name
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.performWith
import io.koalaql.test.data.DataTypeValuesMap
import io.koalaql.test.data.DataTypeWithValues
import io.koalaql.transact
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class DataTypesTest : ProvideTestDatabase {
    abstract fun compatibilityAdjustment(values: DataTypeValuesMap)

    private fun <T : Any> compareRows(rows: List<T>, values: DataTypeWithValues<T>) {
        if (values.type is VARBINARY) {
            assertEquals(values.values.size, rows.size)

            repeat(rows.size) { ix ->
                val lhs = values.values[ix] as ByteArray
                val rhs = rows[ix] as ByteArray

                assertContentEquals(lhs, rhs)
            }
        } else {
            assertListEquals(values.values, rows, "$values $rows")
        }
    }

    private fun <T : Any> selectData(cxn: DataConnection, values: DataTypeWithValues<T>) {
        val label = Name(values.type.type, IdentifierName())
        val casted = cast(cast(label, values.type), values.type)

        val rows = values(values.values) { this[label] = it }
            .orderBy(casted)
            .select(casted as_ label)
            .performWith(cxn)
            .map { it.getValue(label) }
            .toList()

        compareRows(rows, values)
    }

    private fun examples() = DataTypeValuesMap().apply {
        val smallStrings = listOf(
            "",
            "A\uD83D\uDCA3\uD83D\uDCA3\uD83D\uDCA3",
            "B\"; OhhHH NooO",
            "Small text example (need to test big text too)"
        )

        this[DECIMAL(4, 2)] = listOf(BigDecimal("12.53"))
        this[DECIMAL(5, 4)] = listOf(BigDecimal("1.5334"), BigDecimal("9.2320"))
        this[BIGINT] = listOf(Long.MIN_VALUE, 0, 257387852879999, Long.MAX_VALUE)
        this[BOOLEAN] = listOf(false, true)
        this[DATE] = listOf(LocalDate.parse("1481-11-21"), LocalDate.parse("1981-04-12"))
        this[DATETIME] = listOf(LocalDateTime.parse("1481-11-21T09:12:43"), LocalDateTime.parse("2481-11-21T09:12:43"))
        this[DOUBLE] = listOf(0.0, Double.MIN_VALUE, 2.0, Double.MAX_VALUE)
        this[FLOAT] = listOf(-482824.0f, 0.0f, Float.MIN_VALUE, 1.4322f, 2.0f, 4853.0f, Float.MAX_VALUE)
        this[TIMESTAMP] = listOf(LocalDateTime.parse("1481-11-21T09:12:43"), LocalDateTime.parse("2481-11-21T09:12:43"))
            .map { it.toInstant(ZoneOffset.UTC) }
        this[INTEGER] = listOf(Int.MIN_VALUE, -600000, 1, 4, 10, 3020, Int.MAX_VALUE)
        this[SMALLINT] = listOf(Short.MIN_VALUE, 8888, Short.MAX_VALUE)
        this[TEXT] = smallStrings
        this[TIME(5)] = listOf(LocalTime.MIDNIGHT, LocalTime.of(10, 10, 10), LocalTime.parse("23:59:59.999990"))
        this[TINYINT] = listOf(-128, 1, 34, 127)
        this[TINYINT.UNSIGNED] = this[TINYINT].map { it.toUByte() }.sorted()
        this[SMALLINT.UNSIGNED] = this[SMALLINT].map { it.toUShort() }.sorted()
        this[INTEGER.UNSIGNED] = this[INTEGER].map { it.toUInt() }.sorted()
        this[BIGINT.UNSIGNED] = this[BIGINT].map { it.toULong() }.sorted()
        this[VARCHAR(100)] = smallStrings
        this[VARBINARY(200)] = smallStrings.map { it.toByteArray() }
        this[RAW<BigDecimal>("DECIMAL(5, 4)")] = this[DECIMAL(5, 4)]

        compatibilityAdjustment(this)
    }

    @Test
    fun `as values, cast to self and order by`() = withCxn { cxn, _ ->
        examples().entries().forEach {
            selectData(cxn, it)
        }
    }

    private inner class DataTable<T : Any>(
        ix: Int,
        val case: DataTypeWithValues<T>
    ): Table("Data$ix") {
        val column = column("test", case.type)

        fun insertData(db: JdbcDataSource) {
            db.transact { cxn -> this
                .insert(values(case.values) {
                    this[column] = it
                })
                .performWith(cxn)

                val rows = this
                    .orderBy(column)
                    .select(column)
                    .performWith(cxn)
                    .map { it.getValue(column) }
                    .toList()

                compareRows(rows, case)
            }
        }
    }

    @Test
    fun `insert into table and read back`() = withDb { db ->
        val cases = examples().entries()

        val tables = cases.mapIndexed { ix, case ->
            DataTable(ix, case)
        }

        db.declareTables(*tables.toTypedArray())

        tables.forEach { it.insertData(db) }
    }

    @Test
    fun `read and write null`() = withCxn { cxn, _ ->
        examples().entries().forEach {
            val name = Name(it.type.type, IdentifierName())
            val wasNull = name.isNull() as_ name()

            val row = values(listOf(1)) { this[name] = null }
                .subquery()
                .select(name, wasNull)
                .performWith(cxn)
                .single()

            assertNull(row.getOrNull(name))
            assert(row[wasNull])
        }
    }
}