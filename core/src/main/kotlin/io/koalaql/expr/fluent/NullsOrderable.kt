package io.koalaql.expr.fluent

import io.koalaql.expr.NullOrdering
import io.koalaql.expr.Ordinal

interface NullsOrderable<T : Any>: Ordinal<T> {
    private fun nullOrdering(ordering: NullOrdering): Ordinal<T> =
        toOrderKey().copy(nulls = ordering)

    fun nullsFirst(): Ordinal<T> = nullOrdering(NullOrdering.FIRST)
    fun nullsLast(): Ordinal<T> = nullOrdering(NullOrdering.LAST)
}