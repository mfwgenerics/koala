package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.values.RowWriter

class Assignment<T : Any>(
    val reference: Reference<T>,
    val value: Expr<T>
)

class LiteralAssignment<T : Any>(
    val reference: Reference<T>,
    val value: T?
) {
    fun placeIntoRow(row: RowWriter) {
        row.value(reference, value)
    }
}

infix fun <T : Any> Reference<T>.setTo(rhs: Expr<T>): Assignment<T> =
    Assignment(this, rhs)

inline infix fun <reified T : Any> Reference<T>.setTo(rhs: T?): LiteralAssignment<T> =
    LiteralAssignment(this, rhs)