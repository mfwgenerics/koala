package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.Deleted
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.Updated
import mfwgenerics.kotq.query.built.*

interface Selectable: BuildsIntoQueryBody {
    private class Select<T : Any>(
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): SelectedJust<T> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            of.buildQueryBody(),
            references,
            includeAll
        )
    }

    private fun <T : Any> selectInternal(references: List<SelectArgument>, includeAll: Boolean): SelectedJust<T> =
        Select(this, references, includeAll)

    fun selectAll(vararg references: SelectArgument): Subqueryable =
        selectInternal<Nothing>(references.asList(), true)

    fun select(vararg references: SelectArgument): Subqueryable =
        selectInternal<Nothing>(references.asList(), false)

    fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJust<T> =
        selectInternal(listOf(labeled), false)

    fun <T : Any> selectJust(reference: Reference<T>): SelectedJust<T> =
        selectInternal(listOf(reference), false)

    private class Update(
        val of: Selectable,
        val assignments: List<Assignment<*>>
    ): Updated {
        override fun buildUpdate() = BuiltUpdate(
            of.buildQueryBody(),
            assignments
        )
    }

    fun update(vararg assignments: Assignment<*>): Updated =
        Update(this, assignments.asList())

    private class Delete(
        val of: Selectable
    ): Deleted {
        override fun buildDelete() = BuiltDelete(of.buildQueryBody())
    }

    fun delete(): Deleted = Delete(this)
}

