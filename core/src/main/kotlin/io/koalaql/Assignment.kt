package io.koalaql

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.Reference
import io.koalaql.expr.RelvarColumn
import io.koalaql.values.RowWriter

interface Assignment<T : Any> {
    val reference: RelvarColumn<T>
    val expr: Expr<T>
}

class ExprAssignment<T : Any>(
    override val reference: RelvarColumn<T>,
    override val expr: Expr<T>
): Assignment<T>

class LiteralAssignment<T : Any>(
    override val reference: RelvarColumn<T>,
    val value: T?
): Assignment<T> {
    override val expr: Expr<T> get() = Literal(
        reference.type,
        value
    )

    fun placeIntoRow(row: RowWriter) {
        row.set(reference, value)
    }
}

infix fun <T : Any> RelvarColumn<T>.setTo(rhs: Expr<T>): Assignment<T> =
    ExprAssignment(this, rhs)

inline infix fun <reified T : Any> RelvarColumn<T>.setTo(rhs: T?): LiteralAssignment<T> =
    LiteralAssignment(this, rhs)