package io.koalaql.window

import io.koalaql.sql.StandardSql

enum class FrameClauseType(
    override val sql: String
): StandardSql {
    RANGE("RANGE"),
    ROWS("ROWS"),
    GROUPS("GROUPS")
}