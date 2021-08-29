package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuiltUnionOperand

interface SelectedUnionOperand: Subqueryable, UnionOperand {
    override fun buildUnionOperand(): BuiltUnionOperand
}