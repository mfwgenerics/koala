package io.koalaql.docs

import io.koalaql.DataSource

data class ExampleData(
    val db: DataSource,

    val hardwareStoreId: Int,
    val groceryStoreId: Int
)
