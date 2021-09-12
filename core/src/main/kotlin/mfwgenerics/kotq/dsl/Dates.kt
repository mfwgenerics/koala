package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.OperationType
import java.time.Instant

fun currentTimestamp(): Expr<Instant> =
    OperationType.CURRENT_TIMESTAMP()
