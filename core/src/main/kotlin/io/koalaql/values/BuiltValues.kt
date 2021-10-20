package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

class BuiltValues(
    override val columns: LabelList,
    private val values: List<Expr<*>?>
): ValuesRow {
    override fun <T : Any> get(reference: Reference<T>): Expr<T> {
        val ix = columns.positionOf(reference) ?: return Literal(reference.type, null)
        if (ix >= values.size) return Literal(reference.type, null)

        @Suppress("unchecked_cast")
        return (values[ix] ?: return Literal(reference.type, null)) as Expr<T>
    }
}