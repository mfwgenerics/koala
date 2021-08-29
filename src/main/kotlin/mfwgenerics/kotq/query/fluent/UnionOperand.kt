package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuiltUnionOperand

interface UnionOperand {
    fun buildUnionOperand(): BuiltUnionOperand
}