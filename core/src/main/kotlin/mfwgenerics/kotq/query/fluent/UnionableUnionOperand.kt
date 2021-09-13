package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltUnionOperand

interface UnionableUnionOperand: Unionable, UnionOperand, BuildsIntoQueryBody {
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
            of.buildQueryBody(),
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
        BuiltQueryUnionOperand(buildQueryBody())

    override fun select(vararg references: SelectArgument): SelectedUnionOperand =
        SelectUnionableUnionOperand<Nothing>(this, references.asList())

    override fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJustUnionOperand<T> =
        SelectUnionableUnionOperand(this, listOf(labeled))

    override fun <T : Any> selectJust(reference: Reference<T>): SelectedJustUnionOperand<T> =
        SelectUnionableUnionOperand(this, listOf(reference))
}