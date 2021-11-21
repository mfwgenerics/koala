package io.koalaql.docs

import io.koalaql.DataSource
import io.koalaql.sql.SqlText

class ExampleData(
    val db: DataSource,

    val hardwareStoreId: Int,
    val groceryStoreId: Int,

    val logged: MutableList<SqlText>
)
