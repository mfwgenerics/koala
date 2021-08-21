package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.values.PreLabeledRow
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.ValuesRow
import java.sql.ResultSet

class AdaptedResultSet(
    override val labels: LabelList,
    val resultSet: ResultSet
): RowIterator {
    override fun <T : Any> get(reference: Reference<T>): T? {
        val ix = labels.positionOf(reference) ?: return null

        @Suppress("unchecked_cast")
        return resultSet.getObject(ix + 1) as T?
    }

    override fun next(): Boolean =
        resultSet.next()

    override fun consume(): ValuesRow {
        val result = PreLabeledRow(labels)

        labels.values.forEach {
            @Suppress("unchecked_cast")
            result.value(it as Reference<Any>, get(it))
        }

        return result
    }
}