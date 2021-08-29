package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.Literal

data class SqlText(
    val sql: String,
    val parameters: List<Literal<*>>
) {
    override fun toString(): String = "$sql\n${parameters.joinToString(", ")}"
}