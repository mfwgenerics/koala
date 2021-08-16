package mfwgenerics.kotq.expr

class Constant<T : Any>(
    val value: T
): Expr<T>

infix fun <T : Any> Expr<T>.eq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.EQ, listOf(this, rhs))
infix fun <T : Any> Expr<T>.neq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.NEQ, listOf(this, rhs))

infix fun <T : Any> Expr<T>.less(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.LT, listOf(this, rhs))
infix fun <T : Any> Expr<T>.lessOrEq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.LTE, listOf(this, rhs))

infix fun <T : Any> Expr<T>.greater(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.GT, listOf(this, rhs))
infix fun <T : Any> Expr<T>.greaterOrEq(rhs: Expr<T>): Expr<Boolean> = OperationExpr(OperationType.GTE, listOf(this, rhs))

infix fun Expr<Boolean>.and(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.AND, listOf(this, rhs))
infix fun Expr<Boolean>.or(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.OR, listOf(this, rhs))

fun not(expr: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.NOT, listOf(expr))

fun <T : Any> Expr<T>.isNull(): Expr<Boolean> = OperationExpr(OperationType.IS_NULL, listOf(this))
fun <T : Any> Expr<T>.isNotNull(): Expr<Boolean> = OperationExpr(OperationType.IS_NOT_NULL, listOf(this))

fun <T : Any> constant(value: T): Expr<T> = Constant(value)

