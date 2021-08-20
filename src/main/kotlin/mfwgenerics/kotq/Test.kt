package mfwgenerics.kotq

import mfwgenerics.kotq.dialect.h2.H2Dialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.values.values
import mfwgenerics.kotq.window.*

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {


    //val selected = selected(TestTable.column1)

    val cte = Alias(IdentifierName("cte0"))
    val cte2 = Alias(IdentifierName("cte1"))

    val selfJoined = Alias(IdentifierName("selfJoined"))
    val renamed = name<Int>("renamed")
    val renamed2 = name<Int>("renamed2")
    val grouped = name<Int>()

    val window = window()
    val window2 = window()

    val grouped2 = name<Int>()

    val test = TestTable
        .with(cte `as` TestTable
            .where(TestTable.column1 eq 1)
            .where(selfJoined[TestTable.column1] eq 1)
            .orderBy(TestTable.column1)
            .select(TestTable.column1 `as` TestTable.column1),
            cte2 `as` TestTable
            .select(TestTable.column1 `as` renamed2)
        )
        .innerJoin(TestTable.alias(selfJoined), selfJoined[TestTable.column1] eq TestTable.column1)
        .where(literal(true).or(TestTable.column1 eq 1))
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .window(window `as` all()
            .partitionBy(literal(1))
            .orderBy(TestTable.column1, cte[TestTable.column1]),
            window2 `as` all()
            .orderBy(TestTable.column1, cte[TestTable.column1]))
        .union(TestTable)
        .orderBy(TestTable.column1, TestTable.column1.desc())
        .offset(20)
        .limit(5)
        .forShare()
        .select(
            max(TestTable.column1)
                over(window)
                `as` grouped2,
            max(distinct(TestTable.column1))
                .filter(literal(1) eq 1)
                over(all()
                    .partitionBy(literal(1))
                    .orderBy(TestTable.column1)
                    .rows()
                    .between(preceding(10), unbounded())
                )
                `as` grouped,
            TestTable,
            selfJoined[TestTable.column1],
            TestTable.column1 `as` renamed
        )

    //println(H2Dialect().compileQueryable(test))

    val alias = Alias(IdentifierName("Y"))

    println(
        H2Dialect().compile(
            TestTable
                .select(TestTable.column1)
                .subquery()
                .alias(alias)
                .select(alias[TestTable.column1])
                .buildQuery()
        )
    )
}