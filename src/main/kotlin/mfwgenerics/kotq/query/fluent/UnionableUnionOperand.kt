package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuildsIntoSelectBody
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSubquery

interface UnionableUnionOperand: Unionable, UnionOperand, BuildsIntoSelectBody {
    private class SelectUnionableUnionOperand(
        val of: UnionableUnionOperand,
        val references: List<Labeled<*>>
    ): SelectedUnionOperand {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.selected = references
            out.columns = LabelList(references.map { it.name })
            return of
        }
    }

    override fun select(vararg references: NamedExprs): SelectedUnionOperand =
        SelectUnionableUnionOperand(this, references.asSequence().flatMap { it.namedExprs() }.toList())
}