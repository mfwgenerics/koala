package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.JdbcTypeMappings
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.values.PreLabeledRow
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.ValuesRow
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