package io.koalaql.sql

enum class IdentifierQuoteStyle(
    val quote: String
) {
    BACKTICKS("`"),
    DOUBLE("\"")
}