package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Literal
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.values.RowWriter

interface Assignment<T : Any> {
    val reference: Reference<T>
    val expr: Expr<T>
}

class ExprAssignment<T : Any>(
    override val reference: Reference<T>,
    override val expr: Expr<T>
): Assignment<T>

class LiteralAssignment<T : Any>(
    override val reference: Reference<T>,
    val value: T?
): Assignment<T> {
    override val expr: Expr<T> get() = Literal(
        reference.type,
        value
    )

    fun placeIntoRow(row: RowWriter) {
        row.value(reference, value)
    }
}

infix fun <T : Any> Reference<T>.setTo(rhs: Expr<T>): Assignment<T> =
    ExprAssignment(this, rhs)

inline infix fun <reified T : Any> Reference<T>.setTo(rhs: T?): LiteralAssignment<T> =
    LiteralAssignment(this, rhs)