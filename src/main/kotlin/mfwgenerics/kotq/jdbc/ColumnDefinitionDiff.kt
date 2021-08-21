package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.ddl.ColumnType

data class ColumnDefinitionDiff(
    val type: ColumnType<*>?,
    val notNull: Boolean?,
    val default: ChangedDefault?,
    val isAutoIncrement: Boolean?
)