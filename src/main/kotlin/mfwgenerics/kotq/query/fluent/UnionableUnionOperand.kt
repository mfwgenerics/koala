package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSubquery

interface UnionableUnionOperand: Unionable, UnionOperand, BuildsIntoSelect {
    private class SelectUnionableUnionOperand(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): SelectedUnionOperand {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.buildSelection(references, false)
            return of
        }
    }

    override fun select(vararg references: SelectArgument): SelectedUnionOperand =
        SelectUnionableUnionOperand(this, references.asList())
}