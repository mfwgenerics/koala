package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSubquery

interface UnionableUnionOperand: Unionable, UnionOperand, BuildsIntoSelect {
    private class SelectUnionableUnionOperand(
        val of: UnionableUnionOperand,
        val references: List<NamedExprs>
    ): SelectedUnionOperand {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.buildSelection(references)
            return of
        }
    }

    override fun select(vararg references: NamedExprs): SelectedUnionOperand =
        SelectUnionableUnionOperand(this, references.asList())
}