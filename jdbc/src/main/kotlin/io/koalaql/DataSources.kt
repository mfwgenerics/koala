package io.koalaql

import io.koalaql.event.ConnectionEventWriter

inline fun <R> DataSource.transact(
    isolation: Isolation = Isolation.REPEATABLE_READ,
    events: ConnectionEventWriter = ConnectionEventWriter.Discard,
    operation: (DataConnection) -> R
): R = connect(isolation, events).use { txn ->
    val result = operation(txn)
    txn.commit()
    result
}