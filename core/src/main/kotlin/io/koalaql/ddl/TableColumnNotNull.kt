package io.koalaql.ddl

import io.koalaql.ddl.built.BuiltColumnDef

@Suppress("unchecked_cast")
class TableColumnNotNull<T : Any>(
    table: Table,
    symbol: String,
    builtDef: BuiltColumnDef
): TableColumn<T>(table, symbol, builtDef)