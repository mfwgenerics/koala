package io.koalaql.query.fluent

import io.koalaql.expr.Reference
import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectedExpr
import io.koalaql.query.built.QueryBodyBuilder
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.BuiltSelectQuery
import io.koalaql.query.built.BuiltUnionOperand

interface UnionableUnionOperand: Unionable, UnionOperand, QueryBodyBuilder {
    private class BuiltSelectedUnionOperand(
        val select: BuiltSelectQuery
    ): BuiltUnionOperand {
        override fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery =
            select.reorderToMatchUnion(selected)
    }

    private class SelectUnionableUnionOperand<T : Any>(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): SelectedUnionOperand, SelectedJustUnionOperand<T> {
        override fun buildQuery() = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )

        override fun buildUnionOperand() =
            BuiltSelectedUnionOperand(buildQuery())
    }

    private class BuiltQueryUnionOperand(
        val query: BuiltQueryBody
    ): BuiltUnionOperand {
        override fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery =
            BuiltSelectQuery(query, selected)
    }

    override fun buildUnionOperand(): BuiltUnionOperand =
        BuiltQueryUnionOperand(BuiltQueryBody.from(this))

    override fun select(vararg references: SelectArgument): SelectedUnionOperand =
        SelectUnionableUnionOperand<Nothing>(this, references.asList())

    override fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJustUnionOperand<T> =
        SelectUnionableUnionOperand(this, listOf(labeled))

    override fun <T : Any> selectJust(reference: Reference<T>): SelectedJustUnionOperand<T> =
        SelectUnionableUnionOperand(this, listOf(reference))
}