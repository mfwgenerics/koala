package io.koalaql.ddl

import io.koalaql.IdentifierName
import io.koalaql.ddl.built.BuiltColumnDef
import io.koalaql.ddl.fluent.ColumnDefinition
import io.koalaql.expr.RelvarColumn
import kotlin.reflect.KClass

@Suppress("unchecked_cast")
class TableColumn<T : Any>(
    val table: Table,
    symbol: String,
    def: ColumnDefinition<T>,
    val builtDef: BuiltColumnDef = def.buildColumnDef()
): RelvarColumn<T>(symbol, builtDef.columnType.type as KClass<T>, IdentifierName()) {
    override fun toString(): String = "${table.relvarName}.$symbol"
}