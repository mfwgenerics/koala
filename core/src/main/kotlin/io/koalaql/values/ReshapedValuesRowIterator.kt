package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class ReshapedValuesRowIterator(
    override val columns: LabelList,
    private val source: RowIterator<ValuesRow>
): ValuesRow, RowIterator<ValuesRow> {
    override fun <T : Any> get(reference: Reference<T>): Expr<T> =
        source.row[reference]

    override val row: ValuesRow = this

    private class ReshapedValues(
        override val columns: LabelList,
        private val values: ValuesRow
    ): ValuesRow {
        override fun <T : Any> get(reference: Reference<T>): Expr<T> =
            values[reference]
    }

    override fun takeRow(): ValuesRow =
        ReshapedValues(columns, source.takeRow())

    override fun next(): Boolean = source.next()
    override fun close() = source.close()
}