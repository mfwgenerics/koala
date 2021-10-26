package io.koalaql.ddl

import io.koalaql.IdentifierName
import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.expr.Column
import kotlin.reflect.KClass

@Suppress("unchecked_cast")
open class TableColumn<T : Any>(
    val table: Table,
    symbol: String,
    val builtDef: BuiltColumnDef
): Column<T>(symbol, builtDef.columnType.type as KClass<T>, IdentifierName()) {
    override fun toString(): String = "${table.tableName}.$symbol"
}