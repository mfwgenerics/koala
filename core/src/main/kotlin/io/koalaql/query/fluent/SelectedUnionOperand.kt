package io.koalaql.query.fluent

import io.koalaql.query.Subqueryable
import io.koalaql.query.built.BuiltSetOperation

interface SelectedUnionOperand: Subqueryable, UnionOperand {
    override fun BuiltSetOperation.buildIntoSetOperation()
}