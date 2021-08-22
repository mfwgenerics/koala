package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.ddl.ColumnType
import mfwgenerics.kotq.ddl.TableColumn

class BuiltColumnDef {
    lateinit var columnType: ColumnType<*>

    var autoIncrement: Boolean = false

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null
}