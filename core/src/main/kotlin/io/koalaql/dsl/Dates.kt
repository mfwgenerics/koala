package io.koalaql.dsl

import io.koalaql.expr.Expr
import io.koalaql.expr.OperationType
import java.time.Instant

fun currentTimestamp(): Expr<Instant> =
    OperationType.CURRENT_TIMESTAMP()
