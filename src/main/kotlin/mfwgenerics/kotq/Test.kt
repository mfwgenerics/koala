package mfwgenerics.kotq

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {
    val tableA = Alias()

    val selected = selected(TestTable.column1)

    TestTable
        .innerJoin(TestTable.alias(tableA), tableA[TestTable.column1] eq TestTable.column1)
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .orderBy(TestTable.column1)
        .offset(20)
        .limit(5)
        .select(TestTable, TestTable.column1, tableA[TestTable.column1], selected)
        .subquery()


    /*

    val alias = Alias()

    val select = object : Select() {
        val test = of(constant(1) )
    }

    MyTable
        .select(MyTable.star(), )

     */
}