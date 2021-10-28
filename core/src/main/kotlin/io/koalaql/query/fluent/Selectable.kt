package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.expr.Reference
import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectedExpr
import io.koalaql.query.Deleted
import io.koalaql.query.Queryable
import io.koalaql.query.Updated
import io.koalaql.query.built.*

interface Selectable: QueryBodyBuilder {
    private class Select<T : Any>(
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): SelectedJust<T> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            includeAll
        )
    }

    private fun <T : Any> selectInternal(references: List<SelectArgument>, includeAll: Boolean): SelectedJust<T> =
        Select(this, references, includeAll)

    fun selectAll(vararg references: SelectArgument): Queryable =
        selectInternal<Nothing>(references.asList(), true)

    fun select(references: List<SelectArgument>): Queryable =
        selectInternal<Nothing>(references, false)

    fun select(vararg references: SelectArgument): Queryable =
        select(references.asList())

    fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJust<T> =
        selectInternal(listOf(labeled), false)

    fun <T : Any> selectJust(reference: Reference<T>): SelectedJust<T> =
        selectInternal(listOf(reference), false)

    private class Update(
        val of: Selectable,
        val assignments: List<Assignment<*>>
    ): Updated {
        override fun buildUpdate() = BuiltUpdate(
            BuiltQueryBody.from(of),
            assignments
        )
    }

    fun update(assignments: List<Assignment<*>>): Updated =
        Update(this, assignments)

    fun update(vararg assignments: Assignment<*>): Updated =
        update(assignments.asList())

    private class Delete(
        val of: Selectable
    ): Deleted {
        override fun buildDelete() = BuiltDelete(BuiltQueryBody.from(of))
    }

    fun delete(): Deleted = Delete(this)
}

