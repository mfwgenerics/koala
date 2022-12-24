package io.koalaql.sql.token

import io.koalaql.expr.Literal

data class LiteralToken(
    val value: Literal<*>
): SqlToken
