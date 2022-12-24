package io.koalaql.h2

import io.koalaql.expr.Literal
import io.koalaql.identifier.Named
import io.koalaql.sql.SqlEscapes

object H2Escapes: SqlEscapes {
    override fun identifier(sql: StringBuilder, identifier: Named) {
        sql.append("\"")
        sql.append(identifier.name)
        sql.append("\"")
    }

    override fun literal(sql: StringBuilder, params: MutableList<Literal<*>>, literal: Literal<*>) {
        sql.append("?")
        params.add(literal)
    }
}