package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.StandardSql

sealed class DataType<T : Any>(
    override val sql: String
): StandardSql {
    object INTEGER : DataType<Int>("INTEGER")
}
