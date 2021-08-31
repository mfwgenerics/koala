package mfwgenerics.kotq.query

import mfwgenerics.kotq.sql.StandardSql

enum class SetOperationType(
    override val sql: String
): StandardSql {
    UNION("UNION"),
    INTERSECTION("INTERSECT"),
    DIFFERENCE("EXCEPT")
}