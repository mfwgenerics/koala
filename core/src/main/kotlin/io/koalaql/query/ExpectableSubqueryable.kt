package io.koalaql.query

import io.koalaql.expr.AsReference
import io.koalaql.expr.ExprQueryable
import io.koalaql.expr.Reference
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.unfoldBuilder
import io.koalaql.values.*

interface ExpectableSubqueryable<out T>: Subqueryable<T> {
    fun BuilderContext.buildQuery(expectedColumns: List<Reference<*>>?): BuiltSubquery

    override fun BuilderContext.buildQuery(): BuiltSubquery = buildQuery(null)

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
    ): Subqueryable<RowOfTwo<A, B>> =
        ExpectingQueryable(this, expectingListOf(first.asReference(), second.asReference())) {
            it.unsafeCastToTwoColumns()
        }

    fun <A : Any, B : Any, C : Any> expecting(
        first: AsReference<A>,
        second: AsReference<B>,
        third: AsReference<C>
    ): Subqueryable<RowOfThree<A, B, C>> =
        ExpectingQueryable(this, expectingListOf(first.asReference(), second.asReference(), third.asReference())) {
            it.unsafeCastToThreeColumns()
        }
}