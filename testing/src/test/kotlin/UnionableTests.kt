import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.*
import io.koalaql.query.Tableless
import io.koalaql.sql.GeneratedSqlException
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class UnionableTests: ProvideTestDatabase {
    private fun castInt(value: Int) = cast(value(value), INTEGER)

    @Test
    fun `union all does not deduplicate`() = withCxn { cxn ->
        val labelOne = label<Int>()

        val results = Tableless
            .select(castInt(1) as_ labelOne)
            .unionAll(select(castInt(1) as_ labelOne))
            .perform(cxn)
            .map { it[labelOne] }
            .toList()

        assertListEquals(listOf(1, 1), results)
    }

    @Test
    fun `sorted union`() = withCxn { cxn ->
        val x = label<Int>()

        val sorted = Tableless
            .select(castInt(3) as_ x)
            .unionAll(select(castInt(1) as_ x))
            .unionAll(select(castInt(2) as_ x))
            .orderBy(x.desc())
            .perform(cxn)
            .map { it[x] }
            .toList()

        assertListEquals(listOf(3, 2, 1), sorted)
    }

    @Test
    fun `union of labelled from cte`() = withCxn { cxn ->
        val x = label<Int>()
        val y = label<String>()

        val cte = cte() as_ values(
            rowOf(
                x setTo cast(value(7), INTEGER),
                y setTo cast(value("Seven"), VARCHAR(200))
            ),
            rowOf(
                x setTo cast(value(8), INTEGER),
                y setTo cast(value("Eight"), VARCHAR(200))
            ),
            rowOf(
                x setTo cast(value(9), INTEGER),
                y setTo cast(value("Nine"), VARCHAR(200))
            )
        )

        val rows = cte
            .where(x eq 9)
            .select(x, y)
            .union(cte.where(x eq 7).select(y))
            .union(cte.where(x eq 8).select(y))
            .orderBy(y)
            .with(cte)
            .perform(cxn)
            .map { row ->
                row.columns.map { row[it] }
            }
            .toList()

        assertEquals(3, rows.size)

        assertListEquals(listOf(null, "Eight"), rows[0])
        assertListEquals(listOf(9, "Nine"), rows[1])
        assertListEquals(listOf(null, "Seven"), rows[2])
    }

    @Test
    fun `order of unions`() = withCxn { cxn ->
        val t0 = object : Table("table0") { }
        val t1 = object : Table("table1") { }
        val t2 = object : Table("table2") { }

        val sql = t0
            .select()
            .union(t1.select())
            .union(t2.select())
            .generateSql(cxn)
            ?.parameterizedSql
            .orEmpty()

        assert(sql.indexOf("table0") < sql.indexOf("table1"))
        assert(sql.indexOf("table1") < sql.indexOf("table2"))
    }

    @Test
    fun `ordered, limited and offset union`() = withCxn { cxn ->
        val x = label<Int>()

        val results = select(castInt(11) as_ x)
            .union(select(castInt(12) as_ x))
            .union(select(castInt(13) as_ x))
            .union(Tableless
                .where(value(false))
                .select(castInt(14) as_ x))
            .union(select(castInt(15) as_ x))
            .union(select(castInt(16) as_ x))
            .orderBy(x)
            .offset(2)
            .limit(3)
            .perform(cxn)
            .map { it.getValue(x) }
            .toList()

        assertListEquals(listOf(13, 15, 16), results)
    }

    @Test
    open fun `except and intersect`() = withCxn { cxn ->
        val x = label<Int>()

        val result = select(castInt(10) as_ x)
            .union(select(castInt(11) as_ x))
            .intersect(select(castInt(10) as_ x))
            .union(select(castInt(12) as_ x))
            .except(select(castInt(10) as_ x))
            .perform(cxn)
            .map { it.getValue(x) }
            .single()

        assertEquals(12, result)
    }

    private object UnionTestTable: Table("UnionTest") {
        val x = column("x", INTEGER)
        val y = column("y", INTEGER)
    }

    @Test
    open fun `insert from a union`() = withCxn(UnionTestTable) { cxn ->
        UnionTestTable
            .insert(select(castInt(20) as_ UnionTestTable.x, castInt(-10) as_ UnionTestTable.y)
                .union(select(30 as_ UnionTestTable.x, -20 as_ UnionTestTable.y))
            )
            .perform(cxn)

        val rows = UnionTestTable
            .unionAll(UnionTestTable)
            .unionAll(select(1 as_ UnionTestTable.x))
            .perform(cxn)
            .flatMap { listOf(
                it.getOrNull(UnionTestTable.x),
                it.getOrNull(UnionTestTable.y)
            ) }
            .toList()

        assertListEquals(
            listOf(20, -10, 30, -20, 20, -10, 30, -20, 1, null),
            rows
        )
    }

    @Test
    open fun `union to values`() = withCxn { cxn ->
        val x = label<Int>()
        val y = label<Float>()
        val z = label<String>()

        val results = select("Test" as_ z, 3.5f as_ y, 3 as_ x)
            .union(values(
                rowOf(
                    x setTo 1,
                    y setTo 1.5f
                ),
                rowOf(
                    x setTo 2,
                    y setTo 2.5f
                )
            ))
            .orderBy(x)
            .perform(cxn)
            .map { row -> row.columns.map { row[it] } }
            .toList()

        assertListEquals(listOf(null, 1.5f, 1), results[0])
        assertListEquals(listOf(null, 2.5f, 2), results[1])
        assertListEquals(listOf("Test", 3.5f, 3), results[2])

        assertEquals(3, results.size)
    }

    @Test
    open fun `expecting of unionable usable as expr`() = withCxn { cxn ->
        val x = label<Int>()

        val expr = select(castInt(5) as_ x)
            .union(select(castInt(7) as_ x))
            .union(select(castInt(9) as_ x))
            .orderBy(x.desc())
            .limit(1)
            .expecting(x)

        val result = select(expr + expr as_ x)
            .perform(cxn)
            .single()
            .first()

        assertEquals(18, result)
    }

    @Test
    open fun `expecting reorders unionable`() = withCxn { cxn ->
        val x = label<Int>()
        val y = label<Int>()

        val rows = select(castInt(5) as_ x, castInt(-2) as_ y)
            .union(select(-4 as_ y, 7 as_ x))
            .union(select(castInt(9) as_ x, -6 as_ y))
            .orderBy(x)
            .expecting(y, x)
            .perform(cxn)
            .flatMap { listOf(it.first(), it.second()) }
            .toList()

        assertListEquals(listOf(-2, 5, -4, 7, -6, 9), rows)
    }

    @Test
    open fun `expecting column mismatch fails`() = withCxn { cxn ->
        val x = label<Int>()
        val y = label<Int>()
        val z = label<Int>()

        try {
            select(castInt(5) as_ x, castInt(-2) as_ y)
                .union(select(-4 as_ y, 7 as_ x))
                .union(select(castInt(9) as_ z, -6 as_ y))
                .orderBy(x)
                .expecting(y, x)
                .perform(cxn)
                .toList()

            assert(false)
        } catch (ex: IllegalStateException) {

        }
    }

    @Test
    open fun `limit on values`() = withCxn { cxn ->
        val x = label<Int>()

        val result = values(rowOf(x setTo castInt(10)), rowOf(x setTo castInt(12)))
            .limit(1)
            .perform(cxn)
            .single()
            .getValue(x)

        val resultExpecting = values(rowOf(x setTo castInt(10)), rowOf(x setTo castInt(12)))
            .limit(1)
            .expecting(x)
            .perform(cxn)
            .single()
            .first()

        assertEquals(10, result)
        assertEquals(10, resultExpecting)
    }

    @Test
    open fun `order by on values`() = withCxn { cxn ->
        val x = label<Int>()
        val y = label<Int>()

        val result =
            values(0..7) {
                this[x] = castInt(-it)
                this[y] = castInt(it + (it % 2)*10)
            }
            .orderBy(y)
            .perform(cxn)
            .flatMap { listOf(it.getValue(x), it.getValue(y)) }
            .toList()

        assertListEquals(
            listOf(0, 0, -2, 2, -4, 4, -6, 6, -1, 11, -3, 13, -5, 15, -7, 17),
            result
        )
    }
}