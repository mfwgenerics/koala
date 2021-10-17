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
            override fun finished(result: Result<Int?>) {
                val message = if (result.isSuccess) {
                    result.getOrNull()
                        ?.let { "succeeded with $it" }
                        ?: "succeeded"
                } else {
                    "failed with ${result.exceptionOrNull()}"
                }

                log("${type.name} $message")
            }

            override fun fullyRead(rows: Int) {
                log("${type.name} finished after $rows")
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