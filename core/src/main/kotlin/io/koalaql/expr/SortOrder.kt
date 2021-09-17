package io.koalaql.expr

import io.koalaql.sql.StandardSql

enum class SortOrder(
    override val sql: String
): StandardSql {
    ASC("ASC"),
    DESC("DESC")
}