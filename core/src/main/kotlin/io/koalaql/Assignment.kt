package io.koalaql

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.RelvarColumn

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
}