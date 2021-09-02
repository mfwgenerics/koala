package mfwgenerics.kotq.sql

enum class IdentifierQuoteStyle(
    val quote: String
) {
    BACKTICKS("`"),
    DOUBLE("\"")
}