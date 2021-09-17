package io.koalaql.expr

import io.koalaql.sql.StandardSql

enum class GroupedOperationType(
    override val sql: String
): StandardSql {
    MAX("MAX"),
    SUM("SUM"),
    COUNT("COUNT")
}