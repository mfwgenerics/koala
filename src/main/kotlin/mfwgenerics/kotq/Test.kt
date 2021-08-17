package mfwgenerics.kotq

import mfwgenerics.kotq.dialect.mysql.MysqlDialect
import mfwgenerics.kotq.expr.*

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {


    //val selected = selected(TestTable.column1)

    val selfJoined = Alias()
    val renamed = name<Int>()

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
            TestTable,
            selfJoined[TestTable.column1],
            TestTable.column1 named renamed
        )

    println(MysqlDialect().compileQueryable(test))
}