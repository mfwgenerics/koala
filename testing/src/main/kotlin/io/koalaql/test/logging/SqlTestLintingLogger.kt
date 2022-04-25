package io.koalaql.test.logging

import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.event.QueryEventWriter
import io.koalaql.sql.CompiledSql

object SqlTestLintingLogger: ConnectionEventWriter {
    private fun lintSql(sql: String) {
        sql.lines().forEach {
            assert(it.isNotBlank()) { sql }
            assert(it == it.trim()) { "Inappropriate whitespace at |$it| in\n$sql" }
            assert("  " !in it) { "Extra space at |$it|\n$sql" }
            assert("""."c0" "c0"""" !in it && """.`c0` `c0`""" !in it) {
                "Redundant relabeling in\n$sql"
            }
        }
    }

    override fun perform(type: ConnectionQueryType, sql: CompiledSql): QueryEventWriter {
        lintSql(sql.parameterizedSql)

        return QueryEventWriter.Discard
    }

    override fun committed(failed: Throwable?) { }

    override fun rollbacked(failed: Throwable?) { }

    override fun closed() { }
}