package io.koalaql.test.shops

import io.koalaql.DataConnection
import io.koalaql.dsl.*

fun createAndPopulate(cxn: DataConnection) {
    val shopIds = ShopTable
        .insert(values(
            rowOf(ShopTable.name setTo "Hardware"),
            rowOf(ShopTable.name setTo "Groceries"),
            rowOf(ShopTable.name setTo "Stationery")
        ))
        .generatingKey(ShopTable.id)
        .perform(cxn)
        .toList()

    val hardwareId = shopIds[0]
    val groceriesId = shopIds[1]
    val stationeryId = shopIds[2]

    val customerIds = CustomerTable
        .insert(values(
            rowOf(
                CustomerTable.firstName setTo "Jane",
                CustomerTable.lastName setTo "Doe"
            ),
            rowOf(
                CustomerTable.firstName setTo "Bob",
                CustomerTable.lastName setTo "Smith"
            )
        ))
        .generatingKey(CustomerTable.id)
        .perform(cxn)
        .toList()

    val janeId = customerIds[0]
    val bobId = customerIds[1]

    val inserted = PurchaseTable
        .insert(values(
            rowOf(
                PurchaseTable.shop setTo groceriesId,
                PurchaseTable.customer setTo janeId,
                PurchaseTable.product setTo "Apple",
                PurchaseTable.price setTo value(150) + 0,
                PurchaseTable.discount setTo 20
            ),
            rowOf(
                PurchaseTable.shop setTo groceriesId,
                PurchaseTable.customer setTo bobId,
                PurchaseTable.product setTo "Pear",
                PurchaseTable.price setTo 200
            ),
            rowOf(
                PurchaseTable.shop setTo hardwareId,
                PurchaseTable.customer setTo janeId,
                PurchaseTable.product setTo "Hammer",
                PurchaseTable.price setTo 8000
            ),
            rowOf(
                PurchaseTable.shop setTo stationeryId,
                PurchaseTable.customer setTo bobId,
                PurchaseTable.product setTo "Pen",
                PurchaseTable.price setTo 500
            ),
        ))
        .perform(cxn)

    assert(4 == inserted)
}