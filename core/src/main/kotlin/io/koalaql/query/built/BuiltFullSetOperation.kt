package io.koalaql.query.built

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType

class BuiltFullSetOperation(
    val type: SetOperationType,
    val distinctness: Distinctness,
    val body: BuiltUnionOperandQuery
)