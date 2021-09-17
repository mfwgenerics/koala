package io.koalaql.jdbc

import io.koalaql.data.JdbcTypeMappings
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList
import io.koalaql.values.PreLabeledRow
import io.koalaql.values.RowIterator
import io.koalaql.values.ValuesRow
import java.sql.ResultSet

class AdaptedResultSet(
    private val typeMappings: JdbcTypeMappings,
    val labels: LabelList,
    val resultSet: ResultSet
): RowIterator, ValuesRow() {
    override val columns: Collection<Reference<*>> get() = labels.values

    override val row: ValuesRow get() = this

    override fun <T : Any> getOrNull(reference: Reference<T>): T? {
        val ix = 1 + (labels.positionOf(reference) ?: return null)

        return typeMappings.mappingFor(reference.type).readJdbc(resultSet, ix)
    }

    override fun next(): Boolean =
        resultSet.next()

    override fun takeRow(): ValuesRow {
        val result = PreLabeledRow(labels)

        labels.values.forEach {
            @Suppress("unchecked_cast")
            result.set(it as Reference<Any>, getOrNull(it))
        }

        return result
    }

    override fun close() { resultSet.close() }
}