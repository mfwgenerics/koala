package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.Updated
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.Relvar
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.*

interface Selectable: BuildsIntoSelect {
    private class Select<T : Any>(
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): SelectedJust<T>, BuildsIntoSelect {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.references = references
            out.includeAll = includeAll
            return of
        }
    }

    private fun <T : Any> selectInternal(references: List<SelectArgument>, includeAll: Boolean): SelectedJust<T> =
        Select(this, references, includeAll)

    fun selectAll(vararg references: SelectArgument): Subqueryable =
        selectInternal<Nothing>(references.asList(), true)

    fun select(vararg references: SelectArgument): Subqueryable =
        selectInternal<Nothing>(references.asList(), false)

    fun <T : Any> select(labeled: SelectedExpr<T>): SelectedJust<T> =
        selectInternal(listOf(labeled), false)

    fun <T : Any> select(reference: Reference<T>): SelectedJust<T> =
        selectInternal(listOf(reference), false)

    private class Update(
        val of: Selectable,
        val assignments: List<Assignment<*>>
    ): Updated {
        override fun buildsIntoUpdate(out: BuiltUpdate): BuildsIntoUpdate? {
            out.select = of.buildSelect()
            out.assignments = assignments
            return null
        }
    }

    fun update(vararg assignments: Assignment<*>): Updated =
        Update(this, assignments.asList())

    /* Relation rather than Table e.g. self join delete may delete by alias */
    fun delete(vararg relations: Relvar): Nothing =
        TODO()
}

