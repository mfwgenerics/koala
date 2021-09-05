package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.ColumnType
import mfwgenerics.kotq.ddl.TableColumn

data class ColumnDiff(
    val newColumn: TableColumn<*>,

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