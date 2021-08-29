package mfwgenerics.kotq.query.fluent

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
            select
    }

    private class SelectUnionableUnionOperand(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): SelectedUnionOperand {
        override fun buildQuery() = BuiltSelectQuery(
            of.buildSelect(),
            references
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
        BuiltQueryUnionOperand(buildSelect())

    override fun select(vararg references: SelectArgument): SelectedUnionOperand =
        SelectUnionableUnionOperand(this, references.asList())
}