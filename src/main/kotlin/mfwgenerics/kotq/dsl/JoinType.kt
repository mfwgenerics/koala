package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.sql.StandardSql

enum class JoinType(
    override val sql: String
): StandardSql {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    OUTER("FULL JOIN")
}