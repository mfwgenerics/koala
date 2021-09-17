package io.koalaql.event

import io.koalaql.sql.SqlText

interface ConnectionEventWriter {
    fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter

    fun committed()
    fun rollbacked()

    fun closed()

    object Discard : ConnectionEventWriter {
        override fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter =
            QueryEventWriter.Discard

        override fun committed() { }
        override fun rollbacked() { }

        override fun closed() { }
    }
}