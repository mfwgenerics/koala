package mfwgenerics.kotq

import mfwgenerics.kotq.ddl.Table

abstract class Database<C : KotqConnection> {
    abstract fun declareTablesUsing(declareBy: DeclareStrategy, tables: List<Table>)

    fun registerTables(vararg tables: Table) { declareTablesUsing(DeclareStrategy.RegisterOnly, tables.asList()) }
    fun createTables(vararg tables: Table) { declareTablesUsing(DeclareStrategy.CreateIfNotExists, tables.asList()) }
    fun declareTables(vararg tables: Table) { declareTablesUsing(DeclareStrategy.Diff, tables.asList()) }

    abstract fun connect(isolation: Isolation): C
    abstract fun close()

    inline fun <R> transact(
        isolation: Isolation = Isolation.REPEATABLE_READ,
        operation: (C) -> R
    ): R {
        val txn = connect(isolation)

        return try {
            val result = operation(txn)
            txn.commit()
            result
        } finally {
            txn.close()
        }
    }
}