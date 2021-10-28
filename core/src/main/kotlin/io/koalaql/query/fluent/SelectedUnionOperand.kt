package io.koalaql.query.fluent

import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltSetOperation

interface SelectedUnionOperand: Queryable, UnionOperand {
    override fun BuiltSetOperation.buildIntoSetOperation()
}