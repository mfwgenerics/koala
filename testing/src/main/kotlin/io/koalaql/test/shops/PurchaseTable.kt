package io.koalaql.test.shops

import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR

object PurchaseTable: Table("Purchase") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val shop = column("shop", INTEGER.foreignKey(ShopTable.id))
    val customer = column("customer", INTEGER.foreignKey(CustomerTable.id))

    val product = column("product", VARCHAR(200))

    val price = column("price", INTEGER)
    val discount = column("discount", INTEGER.nullable())
}