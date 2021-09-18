package io.koalaql.test.logging

import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.ConnectionQueryType
import io.koalaql.event.QueryEventWriter
import io.koalaql.sql.SqlText

class TextEventLogger(
    val id: String
): ConnectionEventWriter {
    private var internalLogs = arrayListOf<String>()

    val logs: List<String> = internalLogs

    private fun log(value: String) {
        internalLogs.add("$id: $value")
    }

    override fun perform(type: ConnectionQueryType, sql: SqlText): QueryEventWriter {
        return object : QueryEventWriter {
            override fun succeeded(rows: Int?) {
                if (rows != null) {
                    log("${type.name} succeeded with rows $rows")
                } else {
                    log("${type.name} succeeded")
                }
            }

            override fun failed(ex: Exception) {
                log("${type.name} failed with $ex")
            }
        }
    }

    override fun committed(failed: Throwable?) {
        if (failed != null) {
            log("commit failed with $failed")
        } else {
            log("commit")
        }
    }

    override fun rollbacked(failed: Throwable?) {
        if (failed != null) {
            log("rollback failed with $failed")
        } else {
            log("rollback")
        }
    }

    override fun closed() { log("close") }
}