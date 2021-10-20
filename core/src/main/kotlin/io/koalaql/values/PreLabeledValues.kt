package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class PreLabeledValues(
    override val columns: LabelList
): ValuesRow, ValuesWriter {
    private val values = arrayOfNulls<Expr<*>>(columns.size)

    fun clear() {
        repeat(values.size) { values[it] = null }
    }

    override fun <T : Any> get(reference: Reference<T>): Expr<T> {
        val ix = columns.positionOf(reference) ?: return Literal(
            reference.type,
            null
        )

        @Suppress("unchecked_cast")
        return (values[ix] ?: return Literal(reference.type, null)) as Expr<T>
    }

    override fun <T : Any> set(reference: Reference<T>, value: Expr<T>) {
        val ix = checkNotNull(columns.positionOf(reference)) {
            "$reference not representable in $columns"
        }

        values[ix] = value
    }
}