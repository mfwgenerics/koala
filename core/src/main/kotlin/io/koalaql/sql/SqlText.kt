package io.koalaql.sql

import io.koalaql.expr.Literal

data class SqlText(
    val parameterizedSql: String,
    val parameters: List<Literal<*>>
) {
    override fun toString(): String = "$parameterizedSql\n${parameters.joinToString(", ")}"
}