package io.koalaql.docs

import io.koalaql.ddl.DECIMAL
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table.Companion.foreignKey
import io.koalaql.ddl.VARCHAR
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.h2.H2DataSource
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.LocalDate

fun ExampleDatabase(): ExampleData {
    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
        }
    )

    db.declareTables(ShopTable, CustomerTable)

    val ids = ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Helen's Hardware Store",
                ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                ShopTable.established setTo LocalDate.parse("1991-02-20")
            ),
            rowOf(
                ShopTable.name setTo "24 Hr Groceries",
                ShopTable.address setTo "1/144 Ronda Drive, Newhaven, 226E",
                ShopTable.established setTo LocalDate.parse("2007-08-02")
            ),
            rowOf(
                ShopTable.name setTo "Permanently Closed Shop",
                ShopTable.address setTo "200 Omar Circle, Xanadu, 91A",
                ShopTable.established setTo LocalDate.parse("2007-08-02")
            )
        ))
        .generatingKey(ShopTable.id)
        .perform(db)
        .toList()

    CustomerTable
        .insert(values(
            rowOf(
                CustomerTable.shop setTo ids[0],
                CustomerTable.name setTo "Michael M. Michael",
                CustomerTable.spent setTo BigDecimal("125.00")
            ),
            rowOf(
                CustomerTable.shop setTo ids[0],
                CustomerTable.name setTo "Maria Robinson",
                CustomerTable.spent setTo BigDecimal("20.50")
            )
        ))
        .perform(db)

    return ExampleData(
        db = db,
        hardwareStoreId = ids[0],
        groceryStoreId = ids[1]
    )
}