package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.fluent.ColumnDefinition
import mfwgenerics.kotq.expr.RelvarColumn
import kotlin.reflect.KClass

@Suppress("unchecked_cast")
class TableColumn<T : Any>(
    val table: Table,
    symbol: String,
    def: ColumnDefinition<T>,
    val builtDef: BuiltColumnDef = def.buildColumnDef()
): RelvarColumn<T>(symbol, builtDef.columnType.type as KClass<T>, IdentifierName())