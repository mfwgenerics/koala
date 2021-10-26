package io.koalaql

import io.koalaql.expr.Expr
import io.koalaql.expr.Literal
import io.koalaql.expr.Column

interface Assignment<T : Any> {
    val reference: Column<T>
    val expr: Expr<T>
}

class ExprAssignment<T : Any>(
    override val reference: Column<T>,
    override val expr: Expr<T>
): Assignment<T>

class LiteralAssignment<T : Any>(
    override val reference: Column<T>,
    val value: T?
): Assignment<T> {
    override val expr: Expr<T> get() = Literal(
        reference.type,
        value
    )
}