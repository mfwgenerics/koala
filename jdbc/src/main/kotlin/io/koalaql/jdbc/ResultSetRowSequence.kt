package io.koalaql.jdbc

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.event.QueryEventWriter
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.sql.TypeMappings
import io.koalaql.values.*
import java.sql.ResultSet

class ResultSetRowSequence(
    override val columns: LabelList,
    private val offset: Int,
    private val event: QueryEventWriter,
    sharedMappings: JdbcTypeMappings,
    localMappings: TypeMappings,
    private val resultSet: ResultSet
):
    RowSequence<RawResultRow>,
    RowIterator<RawResultRow>,
    RawResultRow
{
    private var alreadyIterated = false
    private var readCount = 0

    private val columnMappings = columns.map {
        sharedMappings.deriveForReference(it, localMappings)
    }

    override val row: RawResultRow get() = this

    @Suppress("unchecked_cast")
    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = columns.positionOf(reference) ?: return null

        /* should we lookup mappingFor in advance? */
        return columnMappings[ix].readJdbc(resultSet, ix + offset) as T?
    }

    @Suppress("unchecked_cast")
    override fun get(ix: Int): Any? = columnMappings[ix].readJdbc(resultSet, ix + offset)

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