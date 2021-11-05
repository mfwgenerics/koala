package io.koalaql.event

import io.koalaql.sql.SqlText

class CombinedConnectionEventWriter(
    private val lhs: ConnectionEventWriter,
    private val rhs: ConnectionEventWriter
): ConnectionEventWriter {
    override fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter =
        lhs.perform(type, sql) + rhs.perform(type, sql)

    override fun committed(failed: Throwable?) {
        lhs.committed(failed)
        rhs.committed(failed)
    }

    override fun rollbacked(failed: Throwable?) {
        lhs.rollbacked(failed)
        rhs.rollbacked(failed)
    }

    override fun closed() {
        lhs.closed()
        rhs.closed()
    }
}