import io.koalaql.dsl.*
import io.koalaql.test.shops.CustomerTable
import kotlin.test.Test
import kotlin.test.assertContentEquals

abstract class ReturningTests: ProvideTestDatabase {
    @Test
    fun `returning from insert`() = withCxn(CustomerTable) { cxn ->
        val results = CustomerTable
            .insert(values(
                rowOf(
                    CustomerTable.firstName setTo "A",
                    CustomerTable.lastName setTo "a"
                ),
                rowOf(
                    CustomerTable.firstName setTo "B",
                    CustomerTable.lastName setTo "b"
                )
            ))
            .returning(CustomerTable.id, CustomerTable.lastName)
            .perform(cxn)
            .map { (x, y) -> x to y }
            .toList()

        assertContentEquals(
            listOf(1 to "a", 2 to "b"),
            results
        )
    }

    @Test
    fun `delete returning in CTE`() = withCxn(CustomerTable) { cxn ->
        CustomerTable
            .insert(values(
                rowOf(
                    CustomerTable.firstName setTo "A",
                    CustomerTable.lastName setTo "a"
                ),
                rowOf(
                    CustomerTable.firstName setTo "B",
                    CustomerTable.lastName setTo "b"
                )
            ))
            .perform(cxn)

        val deletedCte = cte() as_ CustomerTable
            .delete()
            .returning(CustomerTable.lastName)

        val results = deletedCte
            .orderBy(CustomerTable.lastName.desc())
            .select(CustomerTable.lastName)
            .with(deletedCte)
            .perform(cxn)
            .map { it.first() }
            .toList()

        assertContentEquals(listOf("b", "a"), results)
    }
}