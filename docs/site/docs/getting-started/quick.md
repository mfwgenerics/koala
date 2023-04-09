---
sidebar_position: 2
---

# Quick example

## Under construction

```kotlin title="Main.kt"
object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val name = column("name", VARCHAR(256))
    val address = column("address", VARCHAR(512))

    val established = column("established", DATE.nullable())
}

fun main() {
    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
        }
    )

    db.declareTables(ShopTable)

    val id = ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Helen's Hardware",
                ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                ShopTable.established setTo LocalDate.parse("1991-02-20")
            )
        ))
        .generatingKey(ShopTable.id)
        .perform(db)
        .single()

    val row = ShopTable
        .where(ShopTable.id eq id)
        .perform(db)
        .single()

    check("Helen's Hardware" == row[ShopTable.name])
}
```
