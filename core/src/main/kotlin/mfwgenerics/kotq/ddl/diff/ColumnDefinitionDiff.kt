package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.ColumnType

data class ColumnDefinitionDiff(
    val type: ColumnType<*>?,
    val notNull: Boolean?,
    val default: ChangedDefault?,
    val isAutoIncrement: Boolean?
) {
    fun doesNothing(): Boolean =
        type == null &&
        notNull == null &&
        default == null &&
        isAutoIncrement == null
}