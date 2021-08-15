package mfwgenerics.kotq

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {
    val tableA = Alias()

    val selected = selected(TestTable.column1)

    val test = TestTable
        .innerJoin(TestTable.alias(tableA), tableA[TestTable.column1] eq TestTable.column1)
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .orderBy(TestTable.column1)
        .offset(20)
        .limit(5)
        .forShare()
        .select(TestTable, TestTable.column1, tableA[TestTable.column1], selected)

    println(test.buildQuery())

    TestTable
        .insertSelect(
            TestTable,
            TestTable.column1 setTo TestTable.column1
        )
}