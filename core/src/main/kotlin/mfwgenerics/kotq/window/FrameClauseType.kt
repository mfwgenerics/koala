package mfwgenerics.kotq.window

import mfwgenerics.kotq.sql.StandardSql

enum class FrameClauseType(
    override val sql: String
): StandardSql {
    RANGE("RANGE"),
    ROWS("ROWS"),
    GROUPS("GROUPS")
}