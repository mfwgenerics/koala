package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.StandardSql

enum class SortOrder(
    override val sql: String
): StandardSql {
    ASC("ASC"),
    DESC("DESC")
}