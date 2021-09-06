package mfwgenerics.kotq

import mfwgenerics.kotq.ddl.Table

abstract class Database<C : KotqConnection> {
    abstract fun declare(vararg tables: Table)

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