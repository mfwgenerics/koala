import mfwgenerics.kotq.ddl.DataType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.createTables
import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.dsl.*
import mfwgenerics.kotq.expr.`as`
import mfwgenerics.kotq.jdbc.ConnectionWithDialect
import mfwgenerics.kotq.jdbc.performWith
import mfwgenerics.kotq.setTo
import java.sql.DriverManager
import kotlin.test.Test

class TestH2 {
    @Test
    fun `triangular numbers from values clause subquery`() {
        val cxn = ConnectionWithDialect(
            H2Dialect(),
            DriverManager.getConnection("jdbc:h2:mem:test")
        )

        val number = name<Int>("number")
        val summed = name<Int>("sumUnder")

        /* need this cast to workaround H2 bug (? in VALUES aren't typed correctly) */
        val castNumber = cast(number, DataType.INT32)

        val alias = alias("A")

        val results = values((1..20).asSequence(), number)
            { value(number, it) }
            .subquery()
            .orderBy(castNumber.desc())
            .select(
                number,
                sum(castNumber)
                    .over(all()
                    .orderBy(castNumber)
                ) `as` summed
            )
            .subquery()
            .alias(alias)
            .where(alias[summed] greater 9)
            .select(alias[number], alias[summed])
            .performWith(cxn)
            .map { row ->
                "${row[alias[number]]}, ${row[alias[summed]]}"
            }
            .joinToString("\n")

        val expected = (1..20)
            .scan(0) { x, y -> x + y }
            .filter { it > 9 }
            .withIndex()
            .reversed()
            .asSequence()
            .map {
                "${it.index + 4}, ${it.value}"
            }
            .joinToString("\n")

        assert(expected == results)

        cxn.jdbc.close()
    }

    @Test
    fun `stringy joins`() {
        val shopTable = object : Table("Shop") {
            val id = column("id", DataType.INT32.autoIncrement())

            val name = column("name", DataType.VARCHAR(100))
        }

        val customerTable = object : Table("Customer") {
            val id = column("id", DataType.INT32.autoIncrement())

            val firstName = column("firstName", DataType.VARCHAR(100))
            val lastName = column("lastName", DataType.VARCHAR(100))
        }

        val purchaseTable = object : Table("Purchase") {
            val id = column("id", DataType.INT32.autoIncrement())

            val shop = column("shop", DataType.INT32.reference(shopTable.id))
            val customer = column("customer", DataType.INT32.reference(customerTable.id))

            val product = column("product", DataType.VARCHAR(200))

            val price = column("price", DataType.INT32)
            val discount = column("discount", DataType.INT32.nullable())
        }

        val cxn = ConnectionWithDialect(
            H2Dialect(),
            DriverManager.getConnection("jdbc:h2:mem:test")
        )

        cxn.ddl(createTables(
            shopTable,
            customerTable,
            purchaseTable
        ))

        val insert = shopTable
            .insert(values(
                rowOf(shopTable.name setTo "Hardware"),
                rowOf(shopTable.name setTo "Groceries"),
                rowOf(shopTable.name setTo "Stationary")
            ))
            .returning(shopTable.id)

        val shopIds = cxn.query(insert)
            .map { it[shopTable.id] }
            .toList()

        val hardwareId = shopIds[0]
        val groceriesId = shopIds[1]
        val stationaryId = shopIds[2]

        shopTable
            .select(shopTable)
            .performWith(cxn)
            .forEach { row ->
                println(row.labels.values.map { row[it] }.joinToString(", "))
            }
    }
}