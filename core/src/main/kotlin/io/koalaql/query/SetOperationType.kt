package io.koalaql.query

import io.koalaql.sql.StandardSql

enum class SetOperationType(
    override val sql: String
): StandardSql {
    UNION("UNION"),
    INTERSECTION("INTERSECT"),
    DIFFERENCE("EXCEPT")
}