package mfwgenerics.kotq

abstract class Database {
    abstract fun transaction(
        isolation: Isolation
    ): Transaction

    inline fun <T> transact(
        isolation: Isolation = Isolation.REPEATABLE_READ,
        operation: (Transaction) -> T
    ): T {
        val txn = transaction(isolation)

        return try {
            val result = operation(txn)
            txn.done()

            result
        } catch (ex: Throwable) {
            txn.done(CommitMode.ROLLBACK)
            throw ex
        }
    }
}