package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.constant

class Assignment<T : Any>(
    val reference: Reference<T>,
    val value: Expr<T>
)

infix fun <T : Any> Reference<T>.setTo(rhs: Expr<T>): Assignment<T> =
    Assignment(this, rhs)

infix fun <T : Any> Reference<T>.setTo(rhs: T): Assignment<T> =
    Assignment(this, constant(rhs))