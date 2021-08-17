package mfwgenerics.kotq

import mfwgenerics.kotq.dialect.mysql.MysqlDialect
import mfwgenerics.kotq.expr.*

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {
    val tableA = Alias("tableA")

    //val selected = selected(TestTable.column1)

    val name = Name<Int>("namebo")

    val test = TestTable
        .innerJoin(TestTable.alias(tableA), tableA[TestTable.column1] eq TestTable.column1)
        .where(constant(true).or(TestTable.column1 eq constant(1)))
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .having(TestTable.column1 eq name)
        /*.union(TestTable
            .groupBy(TestTable.column1)
            .having(TestTable.column1 eq TestTable.column1)
        )*/
        .orderBy(TestTable.column1, TestTable.column1.desc())
        .offset(20)
        .limit(5)
        .forShare()
        .select(
            TestTable,
            tableA[TestTable.column1],
            constant(1) named name
        )

    println(MysqlDialect().compileQueryable(test))
}