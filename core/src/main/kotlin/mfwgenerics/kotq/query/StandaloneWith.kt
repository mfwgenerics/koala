package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.fluent.SelectedJust

class StandaloneWith(
    private val type: WithType,
    private val queries: List<CtedQueryable>
) {
    fun select(vararg references: SelectArgument): Subqueryable =
        StandaloneSelect<Nothing>(references.asList(), false, type, queries)

    fun <T : Any> select(labeled: SelectedExpr<T>): SelectedJust<T> =
        StandaloneSelect(listOf(labeled), false, type, queries)

    fun <T : Any> select(reference: Reference<T>): SelectedJust<T> =
        StandaloneSelect(listOf(reference), false, type, queries)
}