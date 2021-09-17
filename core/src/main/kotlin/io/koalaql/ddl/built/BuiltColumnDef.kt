package io.koalaql.ddl.built

import io.koalaql.data.DataType
import io.koalaql.ddl.IndexType
import io.koalaql.ddl.TableColumn

class BuiltColumnDef {
    lateinit var columnType: DataType<*, *>

    var autoIncrement: Boolean = false

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null

    var markedAsKey: IndexType? = null
}