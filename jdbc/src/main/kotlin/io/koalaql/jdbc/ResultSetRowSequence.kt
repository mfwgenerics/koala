package io.koalaql.jdbc

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.event.QueryEventWriter
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.values.*
import java.sql.ResultSet

class ResultSetRowSequence(
    override val columns: LabelList,
    private val event: QueryEventWriter,
    private val typeMappings: JdbcTypeMappings,
    private val resultSet: ResultSet
):
    RowSequence<RawResultRow>,
    RowIterator<RawResultRow>,
    RawResultRow
{
    private var alreadyIterated = false
    private var readCount = 0

    override val row: RawResultRow get() = this

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = 1 + (columns.positionOf(reference) ?: return null)

        /* should we lookup mappingFor in advance? */
        return typeMappings.mappingFor(reference.type).readJdbc(resultSet, ix)
    }

    @Suppress("unchecked_cast")
    override fun get(ix: Int): Any? {
        val reference = columns[ix] as Reference<Any>

        return typeMappings.mappingFor(reference.type).readJdbc(resultSet, ix + 1)
    }

    override fun next(): Boolean {
        return if (resultSet.next()) {
            readCount++
            true
        } else {
            false
        }
    }

    override fun takeRow(): RawResultRow {
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

    override fun rowIterator(): RowIterator<RawResultRow> {
        check(!alreadyIterated)
            { "rowIterator() can only be called once" }

        alreadyIterated = true

        return this
    }

    override fun toString(): String = "Results(${columns}, read=$readCount)"
}