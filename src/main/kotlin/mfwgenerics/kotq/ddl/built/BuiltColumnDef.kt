package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.ddl.ColumnType
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.expr.Expr

class BuiltColumnDef {
    lateinit var columnType: ColumnType<*>

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null

    var autoIncrement: Boolean = false
}