package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.StandardSql

enum class GroupedOperationType(
    override val sql: String
): StandardSql {
    MAX("MAX"),
    SUM("SUM"),
    COUNT("COUNT")
}