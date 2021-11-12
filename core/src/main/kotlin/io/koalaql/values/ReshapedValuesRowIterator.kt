package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class ReshapedValuesRowIterator(
    override val columns: LabelList,
    private val source: ValuesIterator
): ValuesRow, ValuesIterator {
    override fun <T : Any> get(reference: Reference<T>): Expr<T> =
        source.row[reference]

    override val row: ValuesRow = this

    override fun next(): Boolean = source.next()
}