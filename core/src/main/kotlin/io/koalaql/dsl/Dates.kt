package io.koalaql.dsl

import io.koalaql.expr.Expr
import io.koalaql.expr.StandardOperationType
import java.time.Instant

fun currentTimestamp(): Expr<Instant> =
    StandardOperationType.CURRENT_TIMESTAMP()
