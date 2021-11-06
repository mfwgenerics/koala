import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.*
import io.koalaql.query.Tableless
import kotlin.test.Test

abstract class UnionableTests: ProvideTestDatabase {
    private fun castInt(value: Int) = cast(value(value), INTEGER)

    @Test
    fun `union all does not deduplicate`() = withDb { db ->
        val labelOne = label<Int>()

        val results = Tableless
            .unionAll(select(castInt(1) as_ labelOne))
            .select(castInt(1) as_ labelOne)
            .performWith(db)
            .map { it.first() }
            .toList()

        assertListEquals(listOf(1, 1), results)
    }

    @Test
    fun `sorted union`() = withDb { db ->
        val x = label<Int>()

        val sorted = Tableless
            .unionAll(select(castInt(1) as_ x))
            .unionAll(select(castInt(2) as_ x))
            .orderBy(x.desc())
            .select(castInt(3) as_ x)
            .performWith(db)
            .map { it.first() }
            .toList()

        assertListEquals(listOf(3, 2, 1), sorted)
    }

    @Test
    fun `union of labelled from cte`() = withDb { db ->
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
            .with(cte)
            .where(x eq 9)
            .union(cte.where(x eq 7).select(y))
            .union(cte.where(x eq 8).select(y))
            .orderBy(y)
            .select(x, y)
            .performWith(db)
            .toList()

        println(rows)
    }
}