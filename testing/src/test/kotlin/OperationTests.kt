import io.koalaql.data.DOUBLE
import io.koalaql.data.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import io.koalaql.jdbc.performWith
import java.math.BigDecimal
import kotlin.test.Test

abstract class OperationTests : ProvideTestDatabase {
    open val REQUIRES_MYSQL_WORKAROUND = false

    object NumberTestTable: Table("TestNumbers") {
        val value = column("value", DOUBLE.nullable())
    }

    @Test
    open fun `simple aggregates`() = withCxn(NumberTestTable) { cxn, _ ->
        NumberTestTable
            .insert(values(listOf(1.0, 1.0, 2.0, 3.0, null)) {
                this[NumberTestTable.value] = it
            })
            .performWith(cxn)

        val row = NumberTestTable
            .select(
                avg(NumberTestTable.value) as_ name(),
                avg(distinct(NumberTestTable.value)) as_ name(),
                cast(count(NumberTestTable.value), DOUBLE) as_ name(),
                cast(count(distinct(NumberTestTable.value)), DOUBLE) as_ name(),
                max(NumberTestTable.value) as_ name(),
                max(distinct(NumberTestTable.value)) as_ name(),
                min(NumberTestTable.value) as_ name(),
                min(distinct(NumberTestTable.value)) as_ name(),
                stddevPop(NumberTestTable.value) as_ name(),
                if (REQUIRES_MYSQL_WORKAROUND) {
                    value(BigDecimal("0.816496580927726"))
                } else {
                    stddevPop(distinct(NumberTestTable.value))
                } as_ name(),
                sum(NumberTestTable.value) as_ name(),
                sum(distinct(NumberTestTable.value)) as_ name(),
                varPop(NumberTestTable.value) as_ name(),
                if (REQUIRES_MYSQL_WORKAROUND) {
                    value(BigDecimal("0.6666666666666666"))
                } else {
                    varPop(distinct(NumberTestTable.value))
                } as_ name()
            )
            .performWith(cxn)
            .single()

        val expected = listOf(
            BigDecimal("1.75"), BigDecimal("2"),
            4.0, 3.0, 3.0, 3.0, 1.0, 1.0, BigDecimal("0.82915619758885"),
            BigDecimal("0.816496580927726"), 7.0, 6.0,
            BigDecimal("0.6875"), BigDecimal("0.6666666666666666")
        )

        val actual = row.columns.map {
            val result = row.getOrNull(it)

            if (result is BigDecimal) {
                result.stripTrailingZeros()
            } else {
                result
            }
        }

        assertListEquals(expected, actual)
    }

    object WindowTestTable: Table("TestNumbers") {
        val value = column("value", INTEGER.nullable())
    }

    @Test
    open fun `simple windows`() = withCxn(WindowTestTable) { cxn, _ ->
        WindowTestTable
            .insert(values(0..9) {
                this[WindowTestTable.value] = it
            })
            .performWith(cxn)

        val w = window()

        val windows = listOf(
            Pair(all(), w),
            Pair(all().partitionBy(WindowTestTable.value % 3), w),
            Pair(all().partitionBy(WindowTestTable.value % 3), w.orderBy(WindowTestTable.value.desc()))
        )

        windows.forEach { (window, over) ->
            val results = WindowTestTable
                .window(
                    w as_ window
                )
                .orderBy(WindowTestTable.value)
                .select(
                    WindowTestTable,
                    rowNumber() over over as_ name()
                )
                .performWith(cxn)
                .map { row -> row.columns.map { row.getValue(it) } }
                .toList()

            // TODO asserts
        }
    }
}