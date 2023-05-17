---
sidebar_position: 1
---

# Case Expressions

## Case

Case expressions are created by calling `case`

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val shopType = case(
    ShopTable.id,
    when_(hardwareStoreId).then("HARDWARE"),
    when_(groceryStoreId).then("GROCERIES"),
    else_ = value("OTHER")
) as_ label()

val type = ShopTable
    .where(ShopTable.id eq groceryStoreId)
    .select(shopType)
    .perform(db)
    .single()
    .getValue(shopType)

check(type == "GROCERIES")
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT CASE T0."id"
WHEN 1 THEN 'HARDWARE'
WHEN 2 THEN 'GROCERIES'
ELSE 'OTHER'
END c0
FROM "Shop" T0
WHERE T0."id" = 2
```

</TabItem>
</Tabs>
````

## Empty case

The subject of a case expression can be omitted.
The case expression will then match the first true condition.

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val shopType = case(
    when_(ShopTable.id eq hardwareStoreId).then("HARDWARE"),
    when_(ShopTable.id eq groceryStoreId).then("GROCERIES")
) as_ label()

val type = ShopTable
    .where(ShopTable.id eq groceryStoreId)
    .select(shopType)
    .perform(db)
    .single()
    .getValue(shopType)

check(type == "GROCERIES")
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT CASE
WHEN T0."id" = 1 THEN 'HARDWARE'
WHEN T0."id" = 2 THEN 'GROCERIES'
END c0
FROM "Shop" T0
WHERE T0."id" = 2
```

</TabItem>
</Tabs>
````
