package io.koalaql.dsl

import io.koalaql.Assignment
import io.koalaql.ExprAssignment
import io.koalaql.LiteralAssignment
import io.koalaql.ddl.UnmappedDataType
import io.koalaql.expr.*
import io.koalaql.expr.fluent.ElseableCase
import io.koalaql.expr.fluent.ThenableCase
import io.koalaql.expr.fluent.WhenableCase
import io.koalaql.query.Queryable
import io.koalaql.sql.RawSqlBuilder

infix fun <T : Any> Expr<T>.as_(reference: Reference<T>): SelectedExpr<T> =
    SelectedExpr(this, reference)

/* limiting T to Comparable to prevent shadowing of other as_ */
inline infix fun <reified T : Comparable<*>> T.as_(reference: Reference<T>): SelectedExpr<T> =
    SelectedExpr(value(this), reference)

infix fun <T : Any> Expr<T>.eq(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.EQ(this, rhs)
inline infix fun <reified T : Any> Expr<T>.eq(rhs: T): Expr<Boolean> = eq(value(rhs))

infix fun <T : Any> Expr<T>.neq(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.NEQ(this, rhs)
inline infix fun <reified T : Any> Expr<T>.neq(rhs: T): Expr<Boolean> = neq(value(rhs))

infix fun <T : Any> Expr<T>.less(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.LT(this, rhs)
inline infix fun <reified T : Any> Expr<T>.less(rhs: T): Expr<Boolean> = less(value(rhs))

infix fun <T : Any> Expr<T>.lessOrEq(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.LTE(this, rhs)
inline infix fun <reified T : Any> Expr<T>.lessOrEq(rhs: T): Expr<Boolean> = lessOrEq(value(rhs))

infix fun <T : Any> Expr<T>.greater(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.GT(this, rhs)
inline infix fun <reified T : Any> Expr<T>.greater(rhs: T): Expr<Boolean> = greater(value(rhs))

infix fun <T : Any> Expr<T>.greaterOrEq(rhs: ComparisonOperand<T>): Expr<Boolean> = OperationType.GTE(this, rhs)
inline infix fun <reified T : Any> Expr<T>.greaterOrEq(rhs: T): Expr<Boolean> = greaterOrEq(value(rhs))

infix fun Expr<Boolean>.and(rhs: Expr<Boolean>): Expr<Boolean> = OperationType.AND(this, rhs)
infix fun Expr<Boolean>.or(rhs: Expr<Boolean>): Expr<Boolean> = OperationType.OR(this, rhs)

fun not(expr: Expr<Boolean>): Expr<Boolean> = OperationType.NOT(expr)

fun <T : Any> Expr<T>.isNull(): Expr<Boolean> = OperationType.IS_NULL(this)
fun <T : Any> Expr<T>.isNotNull(): Expr<Boolean> = OperationType.IS_NOT_NULL(this)

fun <T : Any> null_(): Expr<T> = OperationType.NULL()

fun exists(query: Queryable<*>): Expr<Boolean> = OperationType.EXISTS(QueryableOfOne.Wrap<Nothing>(query))
fun notExists(query: Queryable<*>): Expr<Boolean> = OperationType.NOT_EXISTS(QueryableOfOne.Wrap<Nothing>(query))

infix fun <T : Any> Expr<T>.inQuery(query: QueryableOfOne<T>): Expr<Boolean> =
    OperationType.IN(this, query)
infix fun <T : Any> Expr<T>.notInQuery(query: QueryableOfOne<T>): Expr<Boolean> =
    OperationType.NOT_IN(this, query)

infix fun <T : Any> Expr<T>.inExprs(values: Collection<Expr<T>>): Expr<Boolean> = if (values.isEmpty()) {
    value(false)
} else {
    OperationType.IN(this, ExprListExpr(values))
}

infix fun <T : Any> Expr<T>.notInExprs(values: Collection<Expr<T>>): Expr<Boolean> = if (values.isEmpty()) {
    value(true)
} else {
    OperationType.NOT_IN(this, ExprListExpr(values))
}

inline infix fun <reified T : Any> Expr<T>.inValues(values: Collection<T?>): Expr<Boolean> =
    inExprs(values.map { value(it) })
inline infix fun <reified T : Any> Expr<T>.notInValues(values: Collection<T?>): Expr<Boolean> =
    notInExprs(values.map { value(it) })

fun <T : Any> cast(from: Expr<*>, to: UnmappedDataType<T>): Expr<T> =
    CastExpr(from, to)

inline fun <reified T : Any> value(value: T?): Literal<T> =
    Literal(T::class, value)

fun <T : Any> all(subquery: QueryableOfOne<T>): ComparisonOperand<T> =
    ComparedQuery(ComparedQueryType.ALL, subquery.buildQuery())

fun <T : Any> any(subquery: QueryableOfOne<T>): ComparisonOperand<T> =
    ComparedQuery(ComparedQueryType.ANY, subquery.buildQuery())

operator fun <T : Number> Expr<T>.div(rhs: Expr<T>): Expr<T> =
    OperationType.DIVIDE(this, rhs)

inline operator fun <reified T : Number> Expr<T>.div(rhs: T): Expr<T> =
    this / value(rhs)

operator fun <T : Number> Expr<T>.rem(rhs: Expr<T>): Expr<T> =
    OperationType.MOD(this, rhs)

inline operator fun <reified T : Number> Expr<T>.rem(rhs: T): Expr<T> =
    this % value(rhs)

operator fun <T : Number> Expr<T>.times(rhs: Expr<T>): Expr<T> =
    OperationType.MULTIPLY(this, rhs)
inline operator fun <reified T : Number> Expr<T>.times(rhs: T): Expr<T> =
    this * value(rhs)

operator fun <T : Number> Expr<T>.plus(rhs: Expr<T>): Expr<T> =
    OperationType.PLUS(this, rhs)
inline operator fun <reified T : Number> Expr<T>.plus(rhs: T): Expr<T> =
    this + value(rhs)

operator fun <T : Number> Expr<T>.minus(rhs: Expr<T>): Expr<T> =
    OperationType.MINUS(this, rhs)
inline operator fun <reified T : Number> Expr<T>.minus(rhs: T): Expr<T> =
    this - value(rhs)

operator fun <T : Number> Expr<T>.unaryMinus(): Expr<T> =
    OperationType.UNARY_MINUS(this)

fun <T : Any> case(expr: Expr<T>): Case<T> = Case(expr)
fun case(): Case<Boolean> = Case(null)

inline infix fun <reified S : Any> Case<S>.when_(expr: S) = when_(value(expr))
inline infix fun <S : Any, reified T : Any> Case<S>.else_(expr: T) = else_(value(expr))
inline infix fun <S : Any, reified T : Any> CaseWhen<S>.then(expr: T) = then(value(expr))

inline infix fun <reified S : Any, T : Any> WhenableCase<S, T>.when_(expr: S) = when_(value(expr))
inline infix fun <reified T : Any> ElseableCase<T>.else_(expr: T) = else_(value(expr))
inline infix fun <S : Any, reified T : Any> ThenableCase<S, T>.then(expr: T) = then(value(expr))

fun <T : Any> coalesce(expr: Expr<T>, vararg operands: Expr<T>): Expr<T> =
    OperationType.COALESCE(expr, *operands)

infix fun Expr<String>.like(expr: Expr<String>): Expr<Boolean> =
    OperationType.LIKE(this, expr)

infix fun Expr<String>.like(expr: String) = like(value(expr))

fun <T : Any> rawExpr(build: RawSqlBuilder.() -> Unit): Expr<T> =
    RawExpr(build)

infix fun <T : Any> Reference<T>.setTo(rhs: Expr<T>): Assignment<T> =
    ExprAssignment(this, rhs)

inline infix fun <reified T : Any> Reference<T>.setTo(rhs: T?): LiteralAssignment<T> =
    LiteralAssignment(this, rhs)

fun lower(expr: Expr<String>): Expr<String> = OperationType.LOWER(expr)
fun upper(expr: Expr<String>): Expr<String> = OperationType.UPPER(expr)