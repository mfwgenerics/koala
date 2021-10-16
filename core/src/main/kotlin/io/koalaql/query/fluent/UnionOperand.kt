package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltSetOperation

interface UnionOperand {
    fun BuiltSetOperation.buildIntoSetOperation()
}