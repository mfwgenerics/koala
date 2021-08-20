package mfwgenerics.kotq.expr

import kotlin.reflect.KClass

class Literal<T : Any>(
    val type: KClass<T>,
    val value: T
): Expr<T>

infix fun <T : Any> Expr<T>.eq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.EQ, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.eq(rhs: T): Expr<Boolean> = eq(literal(rhs))

infix fun <T : Any> Expr<T>.neq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.NEQ, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.neq(rhs: T): Expr<Boolean> = neq(literal(rhs))

infix fun <T : Any> Expr<T>.less(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.LT, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.less(rhs: T): Expr<Boolean> = less(literal(rhs))

infix fun <T : Any> Expr<T>.lessOrEq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.LTE, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.lessOrEq(rhs: T): Expr<Boolean> = lessOrEq(literal(rhs))

infix fun <T : Any> Expr<T>.greater(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.GT, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.greater(rhs: T): Expr<Boolean> = greater(literal(rhs))

infix fun <T : Any> Expr<T>.greaterOrEq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.GTE, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.greaterOrEq(rhs: T): Expr<Boolean> = greaterOrEq(literal(rhs))

infix fun Expr<Boolean>.and(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.AND, listOf(this, rhs))
infix fun Expr<Boolean>.or(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.OR, listOf(this, rhs))

fun not(expr: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.NOT, listOf(expr))

fun <T : Any> Expr<T>.isNull(): Expr<Boolean> = OperationExpr(OperationType.IS_NULL, listOf(this))
fun <T : Any> Expr<T>.isNotNull(): Expr<Boolean> = OperationExpr(OperationType.IS_NOT_NULL, listOf(this))

fun <T : Any> cast(from: Expr<*>, to: DataType<T>): Expr<T> =
    CastExpr(from, to)

inline fun <reified T : Any> literal(value: T): Expr<T> =
    Literal(T::class, value)

