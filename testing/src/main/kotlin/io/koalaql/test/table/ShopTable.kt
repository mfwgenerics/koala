package io.koalaql.test.table

import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.keys

object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement())

    val name = column("name", VARCHAR(100))

    init {
        primaryKey(keys(id))
    }
}