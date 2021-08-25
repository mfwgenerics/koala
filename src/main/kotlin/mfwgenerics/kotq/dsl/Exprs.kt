package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.ddl.DataType
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.fluent.SelectedJust

infix fun <T : Any> Expr<T>.eq(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.EQ, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.eq(rhs: T): Expr<Boolean> = eq(literal(rhs))

infix fun <T : Any> Expr<T>.neq(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.NEQ, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.neq(rhs: T): Expr<Boolean> = neq(literal(rhs))

infix fun <T : Any> Expr<T>.less(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.LT, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.less(rhs: T): Expr<Boolean> = less(literal(rhs))

infix fun <T : Any> Expr<T>.lessOrEq(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.LTE, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.lessOrEq(rhs: T): Expr<Boolean> = lessOrEq(literal(rhs))

infix fun <T : Any> Expr<T>.greater(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.GT, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.greater(rhs: T): Expr<Boolean> = greater(literal(rhs))

infix fun <T : Any> Expr<T>.greaterOrEq(rhs: ComparisonOperand<T>): Expr<Boolean> =
    OperationExpr(OperationType.GTE, listOf(this, rhs))
inline infix fun <reified T : Any> Expr<T>.greaterOrEq(rhs: T): Expr<Boolean> = greaterOrEq(literal(rhs))

infix fun Expr<Boolean>.and(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.AND, listOf(this, rhs))
infix fun Expr<Boolean>.or(rhs: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.OR, listOf(this, rhs))

fun not(expr: Expr<Boolean>): Expr<Boolean> = OperationExpr(OperationType.NOT, listOf(expr))

fun <T : Any> Expr<T>.isNull(): Expr<Boolean> = OperationType.IS_NULL(this)
fun <T : Any> Expr<T>.isNotNull(): Expr<Boolean> = OperationType.IS_NOT_NULL(this)

fun exists(query: Subqueryable): Expr<Boolean> =
    OperationType.EXISTS(SubqueryExpr(query.buildQuery()))

fun notExists(query: Subqueryable): Expr<Boolean> =
    OperationType.NOT_EXISTS(SubqueryExpr(query.buildQuery()))

infix fun <T : Any> Expr<T>.inQuery(query: SelectedJust<T>): Expr<Boolean> =
    OperationType.IN(this, SubqueryExpr(query.buildQuery()))

infix fun <T : Any> Expr<T>.notInQuery(query: SelectedJust<T>): Expr<Boolean> =
    OperationType.NOT_IN(this, SubqueryExpr(query.buildQuery()))

infix fun <T : Any> Expr<T>.inExprs(values: Collection<Expr<T>>): Expr<Boolean> =
    OperationType.IN(this, ExprListExpr(values))

infix fun <T : Any> Expr<T>.notInExprs(values: Collection<Expr<T>>): Expr<Boolean> =
    OperationType.NOT_IN(this, ExprListExpr(values))

inline infix fun <reified T : Any> Expr<T>.inValues(values: Collection<T>): Expr<Boolean> =
    inExprs(values.map { Literal(T::class, it) })

inline infix fun <reified T : Any> Expr<T>.notInValues(values: Collection<T>): Expr<Boolean> =
    notInExprs(values.map { Literal(T::class, it) })

fun <T : Any> cast(from: Expr<*>, to: DataType<T>): Expr<T> =
    CastExpr(from, to)

inline fun <reified T : Any> literal(value: T?): Expr<T> =
    Literal(T::class, value)

fun <T : Any> all(subquery: SelectedJust<T>): ComparisonOperand<T> =
    ComparedQuery(ComparedQueryType.ALL, subquery.buildQuery())

fun <T : Any> any(subquery: SelectedJust<T>): ComparisonOperand<T> =
    ComparedQuery(ComparedQueryType.ANY, subquery.buildQuery())

operator fun Expr<out Number>.div(divisor: Expr<out Number>): Expr<Double> =
    OperationType.DIVIDE(this, divisor)
inline operator fun <reified T : Number> Expr<out Number>.div(divisor: T): Expr<Double> =
    this / literal(divisor)
