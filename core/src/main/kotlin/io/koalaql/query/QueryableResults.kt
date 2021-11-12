package io.koalaql.query

import io.koalaql.expr.AsReference
import io.koalaql.expr.ExprQueryable
import io.koalaql.expr.Reference
import io.koalaql.values.*
import io.koalaql.values.unsafeCastToTwoColumns

interface QueryableResults: Queryable<ResultRow> {
    private fun expectingListOf(vararg elements: Reference<*>): List<Reference<*>> {
        val references = hashSetOf<Reference<*>>()

        elements.forEach {
            check(references.add(it)) {
                "duplicate reference $it in expecting(${elements.joinToString(", ")})"
            }
        }

        return elements.asList()
    }

    fun <A : Any> expecting(
        first: AsReference<A>
    ): ExprQueryable<A> =
        ExpectingExprQueryable(this, expectingListOf(first.asReference())) {
            it.unsafeCastToOneColumn()
        }

    fun <A : Any, B : Any> expecting(
        first: AsReference<A>,
        second: AsReference<B>
    ): Queryable<RowWithTwoColumns<A, B>> =
        ExpectingQueryable(this, expectingListOf(first.asReference(), second.asReference())) {
            it.unsafeCastToTwoColumns()
        }

    fun <A : Any, B : Any, C : Any> expecting(
        first: AsReference<A>,
        second: AsReference<B>,
        third: AsReference<C>
    ): Queryable<RowWithThreeColumns<A, B, C>> =
        ExpectingQueryable(this, expectingListOf(first.asReference(), second.asReference(), third.asReference())) {
            it.unsafeCastToThreeColumns()
        }
}