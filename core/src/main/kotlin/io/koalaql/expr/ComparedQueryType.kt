package io.koalaql.expr

import io.koalaql.sql.StandardSql

enum class ComparedQueryType(
    override val sql: String
): StandardSql {
    ANY("ANY"),
    ALL("ALL")
}