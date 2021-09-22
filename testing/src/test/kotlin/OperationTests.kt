import io.koalaql.data.DOUBLE
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import io.koalaql.jdbc.performWith
import kotlin.test.Test

abstract class OperationTests : ProvideTestDatabase {
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
                stddevPop(distinct(NumberTestTable.value)) as_ name(),
                sum(NumberTestTable.value) as_ name(),
                sum(distinct(NumberTestTable.value)) as_ name(),
                varPop(NumberTestTable.value) as_ name(),
                varPop(distinct(NumberTestTable.value)) as_ name()
            )
            .performWith(cxn)
            .single()

        val expected = listOf(
            1.75, 2.0, 4.0, 3.0, 3.0, 3.0, 1.0, 1.0,0.82915619758885,
            0.816496580927726, 7.0, 6.0, 0.6875, 0.6666666666666666
        )

        val actual = row.columns.map { row.getOrNull(it) as Double? }

        assertListEquals(expected, actual)
    }
}