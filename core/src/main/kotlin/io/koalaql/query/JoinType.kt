package io.koalaql.query

import io.koalaql.sql.StandardSql

enum class JoinType(
    override val sql: String
): StandardSql {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    OUTER("FULL JOIN")
}