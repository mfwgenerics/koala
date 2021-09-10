package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.fluent.SelectedJust

fun select(vararg references: SelectArgument): Subqueryable =
    StandaloneSelect<Nothing>(references.asList(), false)

fun <T : Any> select(labeled: SelectedExpr<T>): SelectedJust<T> =
    StandaloneSelect(listOf(labeled), false)

inline fun <reified T : Any> select(reference: Expr<T>): SelectedJust<T> =
    StandaloneSelect(listOf(reference as_ name<T>()), false)

fun <T : Any> select(reference: Reference<T>): SelectedJust<T> =
    StandaloneSelect(listOf(reference), false)

fun with(vararg queries: CtedQueryable): StandaloneWith =
    StandaloneWith(WithType.NOT_RECURSIVE, queries.asList())

fun withRecursive(vararg queries: CtedQueryable): StandaloneWith =
    StandaloneWith(WithType.RECURSIVE, queries.asList())