package io.koalaql.docs

import io.koalaql.DataSource
import io.koalaql.sql.CompiledSql

class ExampleData(
    val db: DataSource,

    val hardwareStoreId: Int,
    val groceryStoreId: Int,

    val logged: MutableList<CompiledSql>
)
