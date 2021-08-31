import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.dsl.rowOf
import mfwgenerics.kotq.dsl.values
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.jdbc.performWith
import mfwgenerics.kotq.setTo
import java.sql.DriverManager
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestMysql {
    @BeforeTest
    fun clearDatabase() {
        // TODO drop and recreate testdb here
    }

    @Test
    fun connect() {
        val cxn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb","root","my-secret-pw")

        val rs = cxn
            .prepareStatement("SELECT VERSION()")
            .executeQuery()

        while (rs.next()) {
            println(rs.getString(1))
        }

        cxn.close()
    }

    object ShopTable: Table("Shop") {
        val id = column("id", INTEGER.autoIncrement())

        val name = column("name", VARCHAR(100))
    }

    object CustomerTable: Table("Customer") {
        val id = column("id", INTEGER.autoIncrement())

        val firstName = column("firstName", VARCHAR(100))
        val lastName = column("lastName", VARCHAR(100))
    }

    object PurchaseTable: Table("Purchase") {
        val id = column("id", INTEGER.autoIncrement())

        val shop = column("shop", INTEGER.reference(ShopTable.id))
        val customer = column("customer", INTEGER.reference(CustomerTable.id))

        val product = column("product", VARCHAR(200))

        val price = column("price", INTEGER)
        val discount = column("discount", INTEGER.nullable())
    }

    // TODO use an assertion library
    private fun assertListEquals(expected: List<Any?>, actual: List<Any?>) {
        assert(expected.size == actual.size)

        repeat(expected.size) {
            assert(expected[it] == actual[it])
        }
    }

    private fun assertListOfListsEquals(expected: List<List<Any?>>, actual: List<List<Any?>>) {
        assert(expected.size == actual.size)

        repeat(expected.size) {
            assertListEquals(expected[it], actual[it])
        }
    }

    fun createAndPopulate(cxn: ConnectionWithDialect) {
        cxn.ddl(createTables(
            ShopTable,
            CustomerTable,
            PurchaseTable
        ))

        val shopIds = ShopTable
            .insert(values(
                rowOf(ShopTable.name setTo "Hardware"),
                rowOf(ShopTable.name setTo "Groceries"),
                rowOf(ShopTable.name setTo "Stationary")
            ))
            .returning(ShopTable.id)
            .performWith(cxn)
            .map { it[ShopTable.id]!! }
            .toList()

        val hardwareId = shopIds[0]
        val groceriesId = shopIds[1]
        val stationaryId = shopIds[2]

        val customerIds = CustomerTable
            .insert(values(
                rowOf(
                    CustomerTable.firstName setTo "Jane",
                    CustomerTable.lastName setTo "Doe"
                ),
                rowOf(
                    CustomerTable.firstName setTo "Bob",
                    CustomerTable.lastName setTo "Smith"
                )
            ))
            .returning(CustomerTable.id)
            .performWith(cxn)
            .map { it[CustomerTable.id]!! }
            .toList()

        val janeId = customerIds[0]
        val bobId = customerIds[1]

        PurchaseTable
            .insert(values(
                rowOf(
                    PurchaseTable.shop setTo groceriesId,
                    PurchaseTable.customer setTo janeId,
                    PurchaseTable.product setTo "Apple",
                    PurchaseTable.price setTo 150,
                    PurchaseTable.discount setTo 20
                ),
                rowOf(
                    PurchaseTable.shop setTo groceriesId,
                    PurchaseTable.customer setTo bobId,
                    PurchaseTable.product setTo "Pear",
                    PurchaseTable.price setTo 200
                ),
                rowOf(
                    PurchaseTable.shop setTo hardwareId,
                    PurchaseTable.customer setTo janeId,
                    PurchaseTable.product setTo "Hammer",
                    PurchaseTable.price setTo 8000
                ),
                rowOf(
                    PurchaseTable.shop setTo stationaryId,
                    PurchaseTable.customer setTo bobId,
                    PurchaseTable.product setTo "Pen",
                    PurchaseTable.price setTo 500
                ),
            ))
            .performWith(cxn)
    }
}