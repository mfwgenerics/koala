package io.koalaql.query.built

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType

data class BuiltSetOperation(
    val type: SetOperationType,
    val distinctness: Distinctness,
    val body: BuiltUnionOperand
)