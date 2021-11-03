package io.koalaql.docs.tables

import io.koalaql.ddl.DATE
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR

object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val name = column("name", VARCHAR(256))
    val address = column("address", VARCHAR(512))

    val established = column("established", DATE.nullable())
}