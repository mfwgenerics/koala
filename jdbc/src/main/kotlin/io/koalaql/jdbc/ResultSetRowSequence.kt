package io.koalaql.jdbc

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.event.QueryEventWriter
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.values.PreLabeledResults
import io.koalaql.values.ResultRow
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence
import java.sql.ResultSet

class ResultSetRowSequence(
    override val columns: LabelList,
    private val event: QueryEventWriter,
    private val typeMappings: JdbcTypeMappings,
    private val resultSet: ResultSet
): RowSequence<ResultRow>, RowIterator<ResultRow>, ResultRow() {
    private var alreadyIterated = false
    private var readCount = 0

    override val row: ResultRow get() = this

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = 1 + (columns.positionOf(reference) ?: return null)

        return typeMappings.mappingFor(reference.type).readJdbc(resultSet, ix)
    }

    override fun next(): Boolean {
        return if (resultSet.next()) {
            readCount++
            true
        } else {
            false
        }
    }

    override fun takeRow(): ResultRow {
        val result = PreLabeledResults(this@ResultSetRowSequence.columns)

        columns.forEach {
            @Suppress("unchecked_cast")
            result.set(it as Reference<Any>, getOrNull(it))
        }

        return result
    }

    override fun close() {
        event.fullyRead(readCount)
        resultSet.close()
    }

    override fun rowIterator(): RowIterator<ResultRow> {
        check(!alreadyIterated)
        { "rowIterator() can only be called once" }

        alreadyIterated = true

        return this
    }

    override fun toString(): String = "Results(${columns}, read=$readCount)"
}