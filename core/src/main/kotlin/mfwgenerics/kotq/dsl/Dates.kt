package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.OperationType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime

fun currentTimestamp(): Expr<ZonedDateTime> =
    OperationType.CURRENT_TIMESTAMP()

fun currentInstant(): Expr<Instant> = currentTimestamp().toInstant()

fun Expr<ZonedDateTime>.atTimeZone(zone: String): Expr<ZonedDateTime> =
    OperationType.AT_TIME_ZONE(this, value(zone))

fun Expr<LocalDateTime>.withTimeZone(zone: String): Expr<ZonedDateTime> =
    OperationType.AT_TIME_ZONE(this, value(zone))

fun Expr<Instant>.asUtcTime(): Expr<ZonedDateTime> =
    /* raw cast - ZonedDateTime with UTC should have same representation as Instant */
    rawExpr { expr(this@asUtcTime) }

fun Expr<ZonedDateTime>.toInstant(): Expr<Instant> =
    rawExpr { expr(atTimeZone("UTC")) }
