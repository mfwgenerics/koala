package io.koalaql.query.fluent

import io.koalaql.query.Queryable
import io.koalaql.query.built.BuiltSetOperation
import io.koalaql.values.ResultRow

interface SelectedUnionOperand: Queryable<ResultRow>, UnionOperand {
    override fun BuiltSetOperation.buildIntoSetOperation()
}