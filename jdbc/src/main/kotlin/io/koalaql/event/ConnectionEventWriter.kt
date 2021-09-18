package io.koalaql.event

import io.koalaql.sql.SqlText

interface ConnectionEventWriter {
    fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter

    fun committed(failed: Throwable?)
    fun rollbacked(failed: Throwable?)

    fun closed()

    object Discard : ConnectionEventWriter {
        override fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter =
            QueryEventWriter.Discard

        override fun committed(failed: Throwable?) { }
        override fun rollbacked(failed: Throwable?) { }

        override fun closed() { }
    }
}