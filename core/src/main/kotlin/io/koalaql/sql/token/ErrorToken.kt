package io.koalaql.sql.token

data class ErrorToken(
    val message: String
): SqlToken