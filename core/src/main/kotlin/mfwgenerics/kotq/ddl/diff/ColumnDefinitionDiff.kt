package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.ColumnType

data class ColumnDefinitionDiff(
    val type: ColumnType<*>? = null,
    val notNull: Boolean? = null,
    val changedDefault: ChangedDefault? = null,
    val isAutoIncrement: Boolean? = null
) {
    fun doesNothing(): Boolean =
        type == null &&
        notNull == null &&
        changedDefault == null &&
        isAutoIncrement == null
}