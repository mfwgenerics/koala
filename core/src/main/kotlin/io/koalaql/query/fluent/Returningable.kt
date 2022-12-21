package io.koalaql.query.fluent

import io.koalaql.expr.SelectOperand
import io.koalaql.query.ExpectableSubqueryable
import io.koalaql.values.ResultRow

interface Returningable {
    fun returning(references: List<SelectOperand<*>>): ExpectableSubqueryable<ResultRow>

    fun <A : Any> returning(
        first: SelectOperand<A>
    ) = returning(listOf(first)).expecting(first)

    fun <A : Any, B : Any> returning(
        first: SelectOperand<A>,
        second: SelectOperand<B>
    ) = returning(listOf(first, second)).expecting(first, second)

    fun <A : Any, B : Any, C : Any> returning(
        first: SelectOperand<A>,
        second: SelectOperand<B>,
        third: SelectOperand<C>
    ) = returning(listOf(first, second, third)).expecting(first, second, third)
}