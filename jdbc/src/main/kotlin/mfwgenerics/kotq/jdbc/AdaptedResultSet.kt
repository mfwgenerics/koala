package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.TypeMappings
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.values.PreLabeledRow
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.ValuesRow
import java.sql.ResultSet

class AdaptedResultSet(
    private val typeMappings: TypeMappings,
    override val labels: LabelList,
    val resultSet: ResultSet
): RowIterator {
    override fun <T : Any> get(reference: Reference<T>): T? {
        val ix = 1 + (labels.positionOf(reference) ?: return null)

        return typeMappings.convert(reference.type) { converted ->
            val result = when (converted) {
                Int::class -> resultSet.getInt(ix)
                Long::class -> resultSet.getLong(ix)
                Float::class -> resultSet.getFloat(ix)
                Double::class -> resultSet.getDouble(ix)
                else -> resultSet.getObject(ix)
            }

            result?.takeUnless { resultSet.wasNull() }
        }
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