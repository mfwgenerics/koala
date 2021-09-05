package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.data.MappedDataType
import mfwgenerics.kotq.ddl.IndexType
import mfwgenerics.kotq.ddl.TableColumn

class BuiltColumnDef {
    lateinit var columnType: MappedDataType<*, *>

    var autoIncrement: Boolean = false

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null

    var markedAsKey: IndexType? = null
}