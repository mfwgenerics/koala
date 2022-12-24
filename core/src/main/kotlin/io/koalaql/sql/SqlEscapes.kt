package io.koalaql.sql

import io.koalaql.expr.Literal
import io.koalaql.identifier.Named

interface SqlEscapes {
    fun identifier(sql: StringBuilder, identifier: Named)

    fun literal(
        sql: StringBuilder,
        params: MutableList<Literal<*>>,
        literal: Literal<*>
    )
}