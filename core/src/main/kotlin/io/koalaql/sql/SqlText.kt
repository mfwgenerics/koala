package io.koalaql.sql

import io.koalaql.expr.Literal

data class SqlText(
    val sql: String,
    val parameters: List<Literal<*>>
) {
    override fun toString(): String = "$sql\n${parameters.joinToString(", ")}"
}