package mfwgenerics.kotq

import mfwgenerics.kotq.dialect.mysql.MysqlDialect
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.window.all
import mfwgenerics.kotq.window.preceding
import mfwgenerics.kotq.window.unbounded

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {


    //val selected = selected(TestTable.column1)

    val selfJoined = Alias()
    val renamed = name<Int>()
    val grouped = name<Int>()

    val test = TestTable
        .innerJoin(TestTable.alias(selfJoined), selfJoined[TestTable.column1] eq TestTable.column1)
        .where(literal(true).or(TestTable.column1 eq 1))
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .orderBy(TestTable.column1, TestTable.column1.desc())
        .offset(20)
        .limit(5)
        .forShare()
        .select(
            max(distinct(TestTable.column1))
                .filter(literal(1) eq 1)
                .over(all()
                    .partitionBy(literal(1))
                    .orderBy(TestTable.column1)
                    .rows()
                    .between(preceding(10), unbounded())
                )
                named grouped,
            TestTable,
            selfJoined[TestTable.column1],
            TestTable.column1 named renamed
        )

    println(MysqlDialect().compileQueryable(test))
}