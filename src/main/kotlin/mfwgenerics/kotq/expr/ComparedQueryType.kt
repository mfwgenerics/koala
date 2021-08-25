package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.StandardSql

enum class ComparedQueryType(
    override val sql: String
): StandardSql {
    ANY("ANY"),
    ALL("ALL")
}