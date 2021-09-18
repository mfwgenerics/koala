package io.koalaql.jdbc

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.event.QueryEventWriter
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.values.PreLabeledRow
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence
import io.koalaql.values.ValuesRow
import java.sql.ResultSet

class ResultSetRowSequence(
    override val columns: LabelList,
    private val event: QueryEventWriter,
    private val typeMappings: JdbcTypeMappings,
    private val resultSet: ResultSet
): RowSequence {
    val iterator: RowIterator = object : RowIterator, ValuesRow() {
        var readCount = 0

        override val columns: Collection<Reference<*>> get() = this@ResultSetRowSequence.columns.values

        override val row: ValuesRow get() = this

        override fun <T : Any> getOrNull(reference: Reference<T>): T? {
            val ix = 1 + (this@ResultSetRowSequence.columns.positionOf(reference) ?: return null)

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

        override fun takeRow(): ValuesRow {
            val result = PreLabeledRow(this@ResultSetRowSequence.columns)

            columns.forEach {
                @Suppress("unchecked_cast")
                result.set(it as Reference<Any>, getOrNull(it))
            }

            return result
        }

        override fun close() {
            event.finished(readCount)
            resultSet.close()
        }
    }

    override fun rowIterator(): RowIterator = iterator
}