package io.koalaql.test.logging

import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.event.QueryEventWriter
import io.koalaql.sql.SqlText

object SqlTestLintingLogger: ConnectionEventWriter {
    private fun lintSql(sql: String) {
        sql.lines().forEach {
            assert(it.isNotBlank()) { sql }
            assert(it == it.trim()) { "Inappropriate whitespace in line |$it| in\n$sql" }
        }
    }

    override fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter {
        lintSql(sql.parameterizedSql)

        return QueryEventWriter.Discard
    }

    override fun committed(failed: Throwable?) { }

    override fun rollbacked(failed: Throwable?) { }

    override fun closed() { }
}