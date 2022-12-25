package io.koalaql.ddl.built

import io.koalaql.ddl.DataType
import io.koalaql.ddl.IndexType
import io.koalaql.ddl.TableColumn
import io.koalaql.expr.Expr
import io.koalaql.unfoldBuilder

class BuiltColumnDef {
    lateinit var columnType: DataType<*, *>

    var autoIncrement: Boolean = false

    var notNull = true
    var default: BuiltColumnDefault? = null

    var references: TableColumn<*>? = null

    var markedAsKey: IndexType? = null

    var using: ((Expr<*>) -> Expr<*>)? = null

    companion object {
        fun from(builder: ColumnDefBuilder): BuiltColumnDef =
            unfoldBuilder(builder, BuiltColumnDef()) { it.buildIntoColumnDef() }
    }
}