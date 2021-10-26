package io.koalaql

import io.koalaql.ddl.TableColumn

/* we do this rather than taking up another field on Table for unused columns */
class TableColumnList(
    private val columns: List<TableColumn<*>>,
    val unused: List<TableColumn<*>>
): List<TableColumn<*>> by columns {
    fun includingUnused() = columns + unused
}