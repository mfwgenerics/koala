package mfwgenerics.kotq

object TestTable : Table("Test") {
    val column1 = column("test0", ColumnType.INT)
}

fun main() {
    TestTable
        .innerJoin(TestTable, TestTable.column1 eq TestTable.column1)
        .groupBy(TestTable.column1)
        .having(TestTable.column1 eq TestTable.column1)
        .orderBy(TestTable.column1)
        .offset(20)
        .limit(5)
        .select(TestTable, TestTable.column1)


    /*

    val alias = Alias()

    val select = object : Select() {
        val test = of(constant(1) )
    }

    MyTable
        .select(MyTable.star(), )

     */
}