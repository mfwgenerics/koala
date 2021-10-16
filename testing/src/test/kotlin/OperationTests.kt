import io.koalaql.ddl.DOUBLE
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import io.koalaql.jdbc.performWith
import io.koalaql.window.Window
import io.koalaql.window.fluent.WindowOrderable
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

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

    private data class WindowCase(
        val window: Window,
        val over: Window,
        val ordered: Boolean
    )

    open val windowRequiresOrderBy: Boolean get() = false

    private fun assertListOfListsEquals(expected: List<List<Any?>>, actual: List<List<Any?>>) {
        assertEquals(expected.size, actual.size)

        repeat(expected.size) {
            assertListEquals(expected[it], actual[it], "row $it")
        }
    }

    @Test
    open fun `window functions work`() = withCxn(WindowTestTable) { cxn, _ ->
        WindowTestTable
            .insert(values(0..9) {
                this[WindowTestTable.value] = it
            })
            .performWith(cxn)

        val w = window()

        all().partitionBy(WindowTestTable.value)

        val windows = listOf(
            WindowCase(all(), w, false),
            WindowCase(all().partitionBy(WindowTestTable.value % 3), w, false),
            WindowCase(
                all().partitionBy(WindowTestTable.value % 3),
                w.orderBy(WindowTestTable.value.desc()),
                true
            ),
            WindowCase(
                all(),
                w.orderBy(WindowTestTable.value greater value(4)),
                true
            )
        )

        val expected = listOf(
            listOf(listOf(1,1,1,0.0,0.1,1,1,null,null,99,10,20,99,0,0,null),listOf(2,2,2,0.1111111111111111,0.2,1,1,0,null,99,20,30,99,0,-1,-1),listOf(3,3,3,0.2222222222222222,0.3,1,2,10,0,0,30,40,0,0,-2,-1),listOf(4,4,4,0.3333333333333333,0.4,1,2,20,10,10,40,50,10,0,-3,-1),listOf(5,5,5,0.4444444444444444,0.5,1,3,30,20,20,50,60,20,0,-4,-1),listOf(6,6,6,0.5555555555555556,0.6,2,3,40,30,30,60,70,30,0,-5,-1),listOf(7,7,7,0.6666666666666666,0.7,2,4,50,40,40,70,80,40,0,-6,-1),listOf(8,8,8,0.7777777777777778,0.8,2,4,60,50,50,80,90,50,0,-7,-1),listOf(9,9,9,0.8888888888888888,0.9,2,5,70,60,60,90,null,60,0,-8,-1),listOf(10,10,10,1.0,1.0,2,5,80,70,70,null,null,70,0,-9,-1)),
            listOf(listOf(1,1,1,0.0,0.25,1,1,null,null,99,30,60,99,0,0,null),listOf(1,1,1,0.0,0.3333333333333333,1,1,null,null,99,40,70,99,-1,-1,null),listOf(1,1,1,0.0,0.3333333333333333,1,1,null,null,99,50,80,99,-2,-2,null),listOf(2,2,2,0.3333333333333333,0.5,1,2,0,null,99,60,90,99,0,-3,-3),listOf(2,2,2,0.5,0.6666666666666666,1,2,10,null,99,70,null,99,-1,-4,-4),listOf(2,2,2,0.5,0.6666666666666666,1,2,20,null,99,80,null,99,-2,-5,-5),listOf(3,3,3,0.6666666666666666,0.75,2,3,30,0,0,90,null,0,0,-6,-3),listOf(3,3,3,1.0,1.0,2,3,40,10,10,null,null,10,-1,-7,-4),listOf(3,3,3,1.0,1.0,2,3,50,20,20,null,null,20,-2,-8,-5),listOf(4,4,4,1.0,1.0,2,4,60,30,30,null,null,30,0,-9,-3)),
            listOf(listOf(4,4,4,1.0,1.0,2,4,30,60,60,null,null,60,-9,0,-6),listOf(3,3,3,1.0,1.0,2,3,40,70,70,null,null,70,-7,-1,-4),listOf(3,3,3,1.0,1.0,2,3,50,80,80,null,null,80,-8,-2,-5),listOf(3,3,3,0.6666666666666666,0.75,2,3,60,90,90,0,null,90,-9,-3,-6),listOf(2,2,2,0.5,0.6666666666666666,1,2,70,null,99,10,null,99,-7,-4,-4),listOf(2,2,2,0.5,0.6666666666666666,1,2,80,null,99,20,null,99,-8,-5,-5),listOf(2,2,2,0.3333333333333333,0.5,1,2,90,null,99,30,0,99,-9,-6,-6),listOf(1,1,1,0.0,0.3333333333333333,1,1,null,null,99,40,10,99,-7,-7,null),listOf(1,1,1,0.0,0.3333333333333333,1,1,null,null,99,50,20,99,-8,-8,null),listOf(1,1,1,0.0,0.25,1,1,null,null,99,60,30,99,-9,-9,null)),
            listOf(listOf(1,1,1,0.0,0.5,1,1,null,null,99,10,20,99,0,-4,-1),listOf(2,1,1,0.0,0.5,1,1,0,null,99,20,30,99,0,-4,-1),listOf(3,1,1,0.0,0.5,1,2,10,0,0,30,40,0,0,-4,-1),listOf(4,1,1,0.0,0.5,1,2,20,10,10,40,50,10,0,-4,-1),listOf(5,1,1,0.0,0.5,1,3,30,20,20,50,60,20,0,-4,-1),listOf(6,6,2,0.5555555555555556,1.0,2,3,40,30,30,60,70,30,0,-9,-1),listOf(7,6,2,0.5555555555555556,1.0,2,4,50,40,40,70,80,40,0,-9,-1),listOf(8,6,2,0.5555555555555556,1.0,2,4,60,50,50,80,90,50,0,-9,-1),listOf(9,6,2,0.5555555555555556,1.0,2,5,70,60,60,90,null,60,0,-9,-1),listOf(10,6,2,0.5555555555555556,1.0,2,5,80,70,70,null,null,70,0,-9,-1))
        )

        windows.forEachIndexed { ix, case ->
            val orderedOver = if (!case.ordered) {
                (case.over as WindowOrderable).orderBy(WindowTestTable.value)
            } else {
                case.over
            }

            val results = WindowTestTable
                .window(
                    w as_ case.window
                )
                .orderBy(WindowTestTable.value)
                .select(listOfNotNull(
                    rowNumber() over case.over as_ name(),
                    rank() over orderedOver as_ name(),
                    denseRank() over orderedOver as_ name(),
                    percentRank() over orderedOver as_ name(),
                    cumeDist() over orderedOver as_ name(),
                    ntile(2) over orderedOver as_ name(),
                    ntile(5) over orderedOver as_ name(),
                    lag(WindowTestTable.value * 10) over orderedOver as_ name(),
                    lag(WindowTestTable.value * 10, 2) over orderedOver as_ name(),
                    lag(WindowTestTable.value * 10, 2, value(99)) over orderedOver as_ name(),
                    lead(WindowTestTable.value * 10) over orderedOver as_ name(),
                    lead(WindowTestTable.value * 10, 2) over orderedOver as_ name(),
                    lead(WindowTestTable.value * 10, 2, value(99)) over orderedOver as_ name(),
                    firstValue(-WindowTestTable.value) over orderedOver as_ name(),
                    lastValue(-WindowTestTable.value) over orderedOver as_ name(),
                    nthValue(-WindowTestTable.value, 2) over orderedOver as_ name()
                ))
                .performWith(cxn)
                .map { row -> row.columns.map {
                    val result = row.getOrNull(it)

                    if (result is Long) result.toInt() else result
                } }
                .toList()

            assertListOfListsEquals(
                expected[ix],
                results
            )
        }
    }
}