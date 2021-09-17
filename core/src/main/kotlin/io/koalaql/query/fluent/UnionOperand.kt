package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltUnionOperand

interface UnionOperand {
    fun buildUnionOperand(): BuiltUnionOperand
}