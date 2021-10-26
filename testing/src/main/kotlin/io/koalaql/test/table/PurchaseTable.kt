package io.koalaql.test.table

import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR

object PurchaseTable: Table("Purchase") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val shop = column("shop", INTEGER.reference(ShopTable.id))
    val customer = column("customer", INTEGER.reference(CustomerTable.id))

    val product = column("product", VARCHAR(200))

    val price = column("price", INTEGER)
    val discount = column("discount", INTEGER.nullable())
}