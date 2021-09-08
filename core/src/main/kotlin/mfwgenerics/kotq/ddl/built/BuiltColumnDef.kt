package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.ddl.IndexType
import mfwgenerics.kotq.ddl.TableColumn

class BuiltColumnDef {
    lateinit var columnType: DataType<*, *>

    var autoIncrement: Boolean = false

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null

    var markedAsKey: IndexType? = null
}