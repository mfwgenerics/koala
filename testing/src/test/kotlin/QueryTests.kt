import io.koalaql.data.FLOAT
import io.koalaql.data.INTEGER
import io.koalaql.data.VARCHAR
import io.koalaql.ddl.Table
import io.koalaql.dsl.*
import io.koalaql.jdbc.JdbcConnection
import io.koalaql.jdbc.performWith
import io.koalaql.query.Alias
import io.koalaql.query.Tableless
import io.koalaql.query.fluent.OnConflictable
import io.koalaql.query.fluent.OnConflicted
import io.koalaql.setTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class QueryTests: ProvideTestDatabase {
    @Test
    fun `perform values directly`() = withCxn { cxn ->
        val number = name<Int>()

        val result = values((1..20).asSequence(), listOf(number))
            { set(number, it) }
            .performWith(cxn)
            .sumOf { it.getOrNull(number)!! }

        assertEquals(result, 210)
    }

    @Test
    fun `triangular numbers from values clause subquery`() = withCxn { cxn ->
        val number = name<Int>("number")

        /* need this cast to workaround H2 bug (? in VALUES aren't typed correctly) */
        val castNumber = cast(number, INTEGER)

        val summed = sum(castNumber)
            .over(all()
                .orderBy(castNumber)
            ) as_ name("sumUnder")

        val alias = alias("A")

        val results = values((1..20).asSequence(), listOf(number))
            { set(number, it) }
            .orderBy(castNumber.desc())
            .select(
                number,
                summed
            )
            .subquery()
            .as_(alias)
            .where(alias[summed] greater 9)
            .select(alias[number], alias[summed])
            .performWith(cxn)
            .map { row ->
                "${row.getOrNull(alias[number])}, ${row.getOrNull(alias[summed])}"
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

        assertEquals(expected, results)
    }

    object ShopTable: Table("Shop") {
        val id = column("id", INTEGER.autoIncrement())

        val name = column("name", VARCHAR(100))

        init {
            primaryKey(keys(id))
        }
    }

    object CustomerTable: Table("Customer") {
        val id = column("id", INTEGER.autoIncrement())

        val firstName = column("firstName", VARCHAR(100))
        val lastName = column("lastName", VARCHAR(100))

        init {
            primaryKey(keys(id))
        }
    }

    object PurchaseTable: Table("Purchase") {
        val id = column("id", INTEGER.autoIncrement().primaryKey())

        val shop = column("shop", INTEGER.reference(ShopTable.id))
        val customer = column("customer", INTEGER.reference(CustomerTable.id))

        val product = column("product", VARCHAR(200))

        val price = column("price", INTEGER)
        val discount = column("discount", INTEGER.nullable())
    }

    // TODO use an assertion library
    private fun assertListEquals(expected: List<Any?>, actual: List<Any?>) {
        assertEquals(expected.size, actual.size)

        repeat(expected.size) {
            assertEquals(expected[it], actual[it])
        }
    }

    private fun assertListOfListsEquals(expected: List<List<Any?>>, actual: List<List<Any?>>) {
        assertEquals(expected.size, actual.size)

        repeat(expected.size) {
            assertListEquals(expected[it], actual[it])
        }
    }

    fun createAndPopulate(cxn: JdbcConnection) {
        cxn.createTable(
            ShopTable,
            CustomerTable,
            PurchaseTable
        )

        val shopIds = ShopTable
            .insert(values(
                rowOf(ShopTable.name setTo "Hardware"),
                rowOf(ShopTable.name setTo "Groceries"),
                rowOf(ShopTable.name setTo "Stationary")
            ))
            .returning(ShopTable.id)
            .performWith(cxn)
            .map { it.getOrNull(ShopTable.id)!! }
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
            .map { it.getOrNull(CustomerTable.id)!! }
            .toList()

        val janeId = customerIds[0]
        val bobId = customerIds[1]

        val inserted = PurchaseTable
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

        assertEquals(4, inserted)
    }

    @Test
    fun `stringy joins`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val expectedPurchaseItems = listOf(
            listOf("Bob", "Pear", 200),
            listOf("Bob", "Pen", 500),
            listOf("Jane", "Apple", 150),
            listOf("Jane", "Hammer", 8000)
        )

        val actualPurchaseItems = CustomerTable
            .innerJoin(PurchaseTable, CustomerTable.id eq PurchaseTable.customer)
            .orderBy(CustomerTable.firstName)
            .select(CustomerTable.firstName, PurchaseTable.product, PurchaseTable.price)
            .performWith(cxn)
            .map { row -> row.columns.map { row.getOrNull(it) } }
            .toList()

        assertListOfListsEquals(expectedPurchaseItems, actualPurchaseItems)

        val total = name<Int>()

        val actualTotals = CustomerTable
            .innerJoin(PurchaseTable, CustomerTable.id eq PurchaseTable.customer)
            .groupBy(CustomerTable.id)
            .orderBy(total.desc())
            .select(CustomerTable.firstName, sum(PurchaseTable.price) as_ total)
            .performWith(cxn)
            .map { row -> row.columns.map { row.getOrNull(it) } }
            .toList()

        val expectedTotals = listOf(
            listOf("Jane", 8150),
            listOf("Bob", 700),
        )

        assertListOfListsEquals(expectedTotals, actualTotals)

        val whoDidntShopAtHardware = CustomerTable
            .leftJoin(PurchaseTable
                .innerJoin(ShopTable, PurchaseTable.shop eq ShopTable.id)
                .where(ShopTable.name eq "Hardware")
                .select(PurchaseTable, ShopTable)
                .subquery(),
                CustomerTable.id eq PurchaseTable.customer
            )
            .where(PurchaseTable.id.isNull())
            .selectJust(CustomerTable.firstName)
            .performWith(cxn)
            .map { it.getOrNull(CustomerTable.firstName) }
            .single()

        assertEquals("Bob", whoDidntShopAtHardware)

        val mp = alias()

        val expectedMostExpensiveByStore = listOf(
            listOf("Groceries", "Pear"),
            listOf("Hardware", "Hammer"),
            listOf("Stationary", "Pen")
        )

        val actualMostExpensiveByStore = PurchaseTable
            .groupBy(PurchaseTable.shop)
            .select(
                PurchaseTable.shop,
                max(PurchaseTable.price) as_ PurchaseTable.price
            )
            .subquery()
            .as_(mp)
            .innerJoin(PurchaseTable, (mp[PurchaseTable.shop] eq PurchaseTable.shop).and(
                mp[PurchaseTable.price] eq PurchaseTable.price
            ))
            .innerJoin(ShopTable, PurchaseTable.shop eq ShopTable.id)
            .orderBy(ShopTable.name)
            .select(ShopTable, PurchaseTable)
            .performWith(cxn)
            .map { listOf(it.getOrNull(ShopTable.name), it.getOrNull(PurchaseTable.product)) }
            .toList()

        assertListOfListsEquals(expectedMostExpensiveByStore, actualMostExpensiveByStore)
    }

    @Test
    fun `update through not exists`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val updated = CustomerTable
            .where(notExists(PurchaseTable
                .innerJoin(ShopTable, PurchaseTable.shop eq ShopTable.id)
                .where(ShopTable.name eq "Hardware")
                .where(CustomerTable.id eq PurchaseTable.customer)
                .selectJust(PurchaseTable.id)
            ))
            .update(
                CustomerTable.lastName setTo CustomerTable.firstName,
                CustomerTable.firstName setTo "Bawb"
            )
            .performWith(cxn)

        assertEquals(1, updated)

        CustomerTable
            .where(CustomerTable.firstName eq "Bawb")
            .where(CustomerTable.lastName eq "Bob")
            .select(CustomerTable)
            .performWith(cxn)
            .single()
    }

    @Test
    fun `multi update`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val expected = PurchaseTable
            .selectAll()
            .performWith(cxn)
            .count()

        val matched = PurchaseTable
            .update(PurchaseTable.product setTo "Pear")
            .performWith(cxn)

        assertEquals(expected, matched)
    }

    @Test
    fun `insert from select and subquery comparisons`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val (bobId, janeId) = CustomerTable
            .where(CustomerTable.firstName inValues listOf("Bob", "Jane"))
            .orderBy(CustomerTable.firstName)
            .selectJust(CustomerTable.id)
            .performWith(cxn)
            .map { it.getOrNull(CustomerTable.id)!! }
            .toList()

        PurchaseTable
            .insert(PurchaseTable
                .where((PurchaseTable.id eq bobId).and(PurchaseTable.product eq "Pear"))
                .select(
                    PurchaseTable.shop,
                    PurchaseTable.customer,

                    value("NanoPear") as_ PurchaseTable.product,
                    cast(PurchaseTable.price / 100, INTEGER) as_ PurchaseTable.price,

                    PurchaseTable.discount
                )
            )
            .performWith(cxn)

        val janesPurchasePrices = PurchaseTable
            .where(PurchaseTable.customer eq janeId)
            .selectJust(PurchaseTable.price)

        val cheaperThanAll = PurchaseTable
            .where((PurchaseTable.customer eq bobId)
                .and(PurchaseTable.price less all(janesPurchasePrices))
            )
            .orderBy(PurchaseTable.product)
            .selectJust(PurchaseTable.product)
            .performWith(cxn)
            .map { it.getOrNull(PurchaseTable.product) }
            .toList()

        val cheaperThanAny = PurchaseTable
            .where((PurchaseTable.customer eq bobId)
                .and(PurchaseTable.price less any(janesPurchasePrices))
            )
            .orderBy(PurchaseTable.product)
            .selectJust(PurchaseTable.product)
            .performWith(cxn)
            .map { it.getOrNull(PurchaseTable.product) }
            .toList()

        assertListEquals(cheaperThanAll, listOf("NanoPear"))
        assertListEquals(cheaperThanAny, listOf("NanoPear", "Pear", "Pen"))
    }

    @Test
    fun `join to cte`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val alias = alias()
        val cte = cte()

        val rows = CustomerTable
            .with(cte as_ PurchaseTable
                .select(
                    PurchaseTable,
                    -PurchaseTable.price as_ PurchaseTable.price
                )
            )
            .innerJoin(cte, CustomerTable.id eq PurchaseTable.customer)
            .leftJoin(cte.as_(alias), (CustomerTable.id eq alias[PurchaseTable.customer])
                .and(PurchaseTable.price less -600))
            .orderBy(
                CustomerTable.id,
                PurchaseTable.id,
                alias[PurchaseTable.id]
            )
            .select(cte, CustomerTable.firstName, cte.as_(alias))
            .performWith(cxn)
            .map { row ->
                row.columns.map { row.getOrNull(it) }
            }
            .toList()

        val expected = listOf(
            listOf(1, 2, 1, "Apple", -150, 20, "Jane", null, null, null, null, null, null),
            listOf(3, 1, 1, "Hammer", -8000, null, "Jane", 1, 2, 1, "Apple", -150, 20),
            listOf(3, 1, 1, "Hammer", -8000, null, "Jane", 3, 1, 1, "Hammer", -8000, null),
            listOf(2, 2, 2, "Pear", -200, null, "Bob", null, null, null, null, null, null),
            listOf(4, 3, 2, "Pen", -500, null, "Bob", null, null, null, null, null, null)
        )

        assertListOfListsEquals(
            expected,
            rows
        )
    }

    @Test
    fun `union all and count`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val count = name<Int>()

        val purchaseCount = PurchaseTable
            .selectJust(count(value(1)) as_ count)
            .performWith(cxn)
            .single().getOrNull(count)!!

        val doubleCount = PurchaseTable
            .unionAll(PurchaseTable)
            .selectAll()
            .performWith(cxn)
            .count()

        assert(purchaseCount == 4)
        assert(doubleCount == 8)
    }

    enum class NumberEnum {
        ONE, TWO, THREE
    }

    enum class ColorEnum {
        RED, GREEN, BLUE
    }

    enum class FruitEnum {
        APPLE, BANANA, ORANGE
    }

    object MappingsTable: Table("Customer") {
        val number = column("number", VARCHAR(100).map({ NumberEnum.valueOf(it) }, { "$it" }))
        val color = column("color", VARCHAR(101).map({ ColorEnum.valueOf(it) }, { "$it" }))
        val fruit = column("fruit", INTEGER.map({ FruitEnum.values()[it] }, { it.ordinal }))
    }

    @Test
    fun `inserting and selecting from mapped columns`() = withCxn { cxn ->
        cxn.createTable(MappingsTable)

        MappingsTable
            .insert(values(
                rowOf(
                    MappingsTable.number setTo NumberEnum.TWO,
                    MappingsTable.color setTo ColorEnum.BLUE,
                    MappingsTable.fruit setTo FruitEnum.BANANA
                )
            ))
            .performWith(cxn)

        val result = MappingsTable
            .selectAll()
            .performWith(cxn)
            .single()

        assert(result.getOrNull(MappingsTable.number) == NumberEnum.TWO)
        assert(result.getOrNull(MappingsTable.color) == ColorEnum.BLUE)
        assert(result.getOrNull(MappingsTable.fruit) == FruitEnum.BANANA)
    }

    @Test
    fun `case expressions and raw expr`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val n0 = name<String>()
        val n1 = name<String>()
        val n2 = name<String>()
        val n3 = name<String>()
        val n4 = name<Int>()

        val results = PurchaseTable
            .select(
                case(PurchaseTable.product,
                    when_("Apple") then "aPPLE"
                ) as_ n0,
                case(
                    when_(PurchaseTable.product eq "Apple") then "Apple?",
                    when_(PurchaseTable.product eq "Pen") then "Pen?"
                ) as_ n1,
                case(PurchaseTable.product,
                    when_("Hammer") then "'ammer"
                ) else_ "'lse" as_ n2,
                case(
                    when_(PurchaseTable.product neq "Pear") then "not a pear"
                ) else_ PurchaseTable.product as_ n3,
                rawExpr<Int> {
                    sql("CASE")
                    sql("\nWHEN "); expr(PurchaseTable.product eq "Apple"); sql(" THEN 12")
                    sql("\nWHEN "); expr(PurchaseTable.product eq "Pen"); sql(" THEN 13")
                    sql("\nEND")
                } as_ n4
            )
            .performWith(cxn)
            .map { listOf(it.getOrNull(n0), it.getOrNull(n1), it.getOrNull(n2), it.getOrNull(n3), it.getOrNull(n4)) }
            .toList()

        val expected = listOf(
            listOf("aPPLE", "Apple?", "'lse", "not a pear", 12),
            listOf(null, null, "'lse", "Pear", null),
            listOf(null, null, "'ammer", "not a pear", null),
            listOf(null, "Pen?", "'lse", "not a pear", 13)
        )

        assertListOfListsEquals(expected, results)
    }

    @Test
    fun `standalone coalesce and scalar query`() = withCxn { cxn ->
        val n0 = name<Int>("n0")
        val n1 = name<String>("n1")
        val n3 = name<Int>("n3")

        val valuesQuery = values((1..5).asSequence(), listOf(n0))
            { set(n0, it) }
            .selectJust(sum(cast(n0, INTEGER)) as_ n3)

        val result = select(
                value(12) as_ n0,
                coalesce(value(null), value("String")) as_ n1,
                valuesQuery as_ n3
            )
            .performWith(cxn)
            .single()

        assert(result.getOrNull(n0) == 12)
        assert(result.getOrNull(n1) == "String")
        assert(result.getOrNull(n3) == 15)
    }

    @Test
    fun `deletion with cte`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val cte = cte()

        PurchaseTable
            .with(cte as_ CustomerTable
                .where(selectJust((CustomerTable.lastName eq "Smith") as_ name()))
                .selectAll()
            )
            .where(PurchaseTable.customer inQuery cte.selectJust(CustomerTable.id))
            .delete()
            .performWith(cxn)

        val name = name<Int>()

        val purchases = PurchaseTable
            .selectJust(count(value(1)) as_ name)
            .performWith(cxn)
            .single().getOrNull(name)

        assert(purchases == 2)
    }

    @Test
    fun `unioned tableless selects with out of order labels`() = withCxn { cxn ->
        val n0 = name<Int>()
        val n1 = name<Float>()

        /* cast is H2 workaround for standalone values */
        fun n0(n: Int) = cast(value(n), INTEGER) as_ n0
        fun n1(n: Float) = cast(value(n), FLOAT) as_ n1

        val expected = listOf(
            listOf(10, 0.5f),
            listOf(20, 0.5f),
            listOf(30, 2.0f),
            listOf(40, 0.25f),
            listOf(50, 0.25f)
        )

        val actual = Tableless
            .union(select(n0(10), n1(0.5f)))
            .unionAll(select(n0(20), n1(0.5f)))
            .unionAll(select(n0(30), n1(2.0f)))
            .union(select(n1(0.25f), n0(40)))
            .orderBy(n0)
            .select(n1(0.25f), n0(50))
            .performWith(cxn)
            .map { row -> listOf(row[n0], row[n1]) }
            .toList()

        assertListOfListsEquals(expected, actual)
    }

    @Test
    open fun `factorial recursive CTE`() = withCxn { cxn ->
        val fact = cte()

        val index = name<Long>()
        val value = name<Long>()

        val alias = Alias()

        val expected = listOf<Long>(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880)

        val actual = fact.as_(alias)
            .withRecursive(fact as_ Tableless
                .unionAll(fact
                    .where(index less 9)
                    .select(index + 1 as_ index, ((index + 1)*value) as_ value)
                )
                .select(0L as_ index, 1L as_ value)
            )
            .select(alias[index], alias[value])
            .performWith(cxn)
            .map { it[alias[value]] }
            .toList()

        assertListEquals(expected, actual)
    }

    @Test
    fun `self joins`() = withCxn { cxn ->
        createAndPopulate(cxn)

        val purchases2 = PurchaseTable as_ alias()

        val shopsAlias = alias()

        val actual = PurchaseTable
            .leftJoin(purchases2, PurchaseTable.price less purchases2[PurchaseTable.price])
            .innerJoin(ShopTable as_ shopsAlias, shopsAlias[ShopTable.id] eq PurchaseTable.shop)
            .leftJoin(ShopTable, ShopTable.id eq purchases2[PurchaseTable.shop])
            .orderBy(PurchaseTable.price, purchases2[PurchaseTable.price])
            .selectAll()
            .performWith(cxn)
            .map { row ->
                listOf(
                    row[PurchaseTable.product],
                    row[PurchaseTable.price],
                    row[shopsAlias[ShopTable.name]],
                    row[purchases2[PurchaseTable.product]],
                    row[purchases2[PurchaseTable.price]],
                    row[ShopTable.name],
                )
            }
            .toList()

        val expected = listOf(
            listOf("Apple", 150, "Groceries", "Pear", 200, "Groceries"),
            listOf("Apple", 150, "Groceries", "Pen", 500, "Stationary"),
            listOf("Apple", 150, "Groceries", "Hammer", 8000, "Hardware"),
            listOf("Pear", 200, "Groceries", "Pen", 500, "Stationary"),
            listOf("Pear", 200, "Groceries", "Hammer", 8000, "Hardware"),
            listOf("Pen", 500, "Stationary", "Hammer", 8000, "Hardware"),
            listOf("Hammer", 8000, "Hardware", null, null, null)
        )

        assertListOfListsEquals(expected, actual)
    }

    object MergeTest : Table("EXCLUDED") {
        val id = column("id", INTEGER.autoIncrement().primaryKey())

        val x = column("x", INTEGER)
        val y = column("y", INTEGER)

        val z = column("z", INTEGER)
        val nz = column("nz", INTEGER)

        val conflictKey = uniqueKey(x, y)
    }

    open val requiresOnConflictKey get() = false

    @Test
    open fun `on duplicate update with values`() = withCxn { cxn ->
        cxn.createTable(MergeTest)

        fun OnConflictable.onConflict0(): OnConflicted {
            return if (requiresOnConflictKey) {
                onConflict(MergeTest.conflictKey)
            } else {
                onDuplicate()
            }
        }

        fun expectValueOf(x: Int, y: Int, z: Int, nz: Int) {
            val row = MergeTest
                .where(MergeTest.x eq x)
                .where(MergeTest.y eq y)
                .select(MergeTest.z, MergeTest.nz)
                .performWith(cxn)
                .single()

            assertEquals(z, row.getValue(MergeTest.z))
            assertEquals(nz, row.getValue(MergeTest.nz))
        }

        MergeTest
            .insert(rowOf(
                MergeTest.x setTo 4,
                MergeTest.y setTo 7,
                MergeTest.z setTo 11,
                MergeTest.nz setTo -11
            ))
            .onConflict0().set(MergeTest.z, MergeTest.nz)
            .performWith(cxn)

        expectValueOf(4, 7, 11, -11)

        MergeTest
            .insert(rowOf(
                MergeTest.x setTo 4,
                MergeTest.y setTo 7,
                MergeTest.z setTo 28,
                MergeTest.nz setTo -28
            ))
            .onConflict0().set(MergeTest.z, MergeTest.nz)
            .performWith(cxn)

        expectValueOf(4, 7, 28, -28)

        MergeTest
            .insert(rowOf(
                MergeTest.x setTo 4,
                MergeTest.y setTo 7,
                MergeTest.z setTo 1,
                MergeTest.nz setTo 0
            ))
            .onConflict0().set(MergeTest.z)
            .performWith(cxn)

        expectValueOf(4, 7, 1, -28)

        MergeTest
            .insert(rowOf(
                MergeTest.x setTo 4,
                MergeTest.y setTo 7,
                MergeTest.z setTo 5,
                MergeTest.nz setTo 4
            ))
            .onConflict0().update(
                MergeTest.z setTo MergeTest.z - Excluded[MergeTest.nz]
            )
            .performWith(cxn)

        expectValueOf(4, 7, -3, -28)
    }
}
