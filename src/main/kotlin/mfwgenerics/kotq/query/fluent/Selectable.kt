package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.Updated
import mfwgenerics.kotq.dsl.Relvar
import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.*

interface Selectable: BuildsIntoSelect {
    private class Select<T : Any>(
        val of: Selectable,
        val references: List<Labeled<*>>
    ): SelectedJust<T>, BuildsIntoSelect {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.selected = references
            out.columns = LabelList(references.map { it.name })
            return of
        }
    }

    private fun <T : Any> selectInternal(references: List<NamedExprs>): SelectedJust<T> =
        Select(this, references.asSequence().flatMap { it.namedExprs() }.toList())

    fun select(vararg references: NamedExprs): Subqueryable =
        selectInternal<Nothing>(references.asList())

    fun <T : Any> select(labeled: Labeled<T>): SelectedJust<T> =
        selectInternal(listOf(labeled))

    fun <T : Any> select(reference: Reference<T>): SelectedJust<T> =
        selectInternal(listOf(reference))

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

