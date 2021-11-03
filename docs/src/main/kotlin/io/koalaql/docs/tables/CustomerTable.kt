package io.koalaql.docs.tables

import io.koalaql.ddl.DECIMAL
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR

object CustomerTable: Table("Customer") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val shop = column("shop", INTEGER.foreignKey(ShopTable.id))

    val name = column("name", VARCHAR(256))
    val spent = column("spent", DECIMAL(7, 2))
}