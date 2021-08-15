package mfwgenerics.kotq.sql

data class SqlText(
    val sql: String,
    val parameters: List<*>
) {
    override fun toString(): String = "$sql\n${parameters.joinToString(", ")}"
}