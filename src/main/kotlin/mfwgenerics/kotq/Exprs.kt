package mfwgenerics.kotq

enum class ComparisonType {
    EQ,
    NEQ,
    LT,
    LTE,
    GT,
    GTE
}

class Comparison<T : Any>(
    val type: ComparisonType,
    val lhs: Expr<T>,
    val rhs: Expr<T>
): Expr<Boolean>

class Constant<T : Any>(
    val value: T
): Expr<T>

infix fun <T : Any> Expr<T>.eq(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.EQ, this, rhs)
infix fun <T : Any> Expr<T>.neq(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.NEQ, this, rhs)

infix fun <T : Any> Expr<T>.less(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.LT, this, rhs)
infix fun <T : Any> Expr<T>.lessOrEq(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.LTE, this, rhs)

infix fun <T : Any> Expr<T>.greater(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.GT, this, rhs)
infix fun <T : Any> Expr<T>.greaterOrEq(rhs: Expr<T>): Expr<Boolean> = Comparison(ComparisonType.GTE, this, rhs)

fun <T : Any> constant(value: T): Expr<T> = Constant(value)

fun <T : Any> selected(expr: Expr<T>): Selected<T> = Selected(expr)