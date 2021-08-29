package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.Distinctness
import mfwgenerics.kotq.query.SetOperationType

data class BuiltSetOperation(
    val type: SetOperationType,
    val distinctness: Distinctness,
    val body: BuiltUnionOperand
)