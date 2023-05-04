---
sidebar_position: 3
---

# Joins

## Inner Join

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
CustomerTable
    .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."shop" c1
, T0."name" c2
, T0."spent" c3
, T1."id" c4
, T1."name" c5
, T1."address" c6
, T1."established" c7
FROM "Customer" T0
INNER JOIN "Shop" T1 ON T1."id" = T0."shop"
```

</TabItem>
</Tabs>
````

## Left and right join

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
CustomerTable
    .leftJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)

CustomerTable
    .rightJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."shop" c1
, T0."name" c2
, T0."spent" c3
, T1."id" c4
, T1."name" c5
, T1."address" c6
, T1."established" c7
FROM "Customer" T0
LEFT JOIN "Shop" T1 ON T1."id" = T0."shop"

SELECT T0."id" c0
, T0."shop" c1
, T0."name" c2
, T0."spent" c3
, T1."id" c4
, T1."name" c5
, T1."address" c6
, T1."established" c7
FROM "Customer" T0
RIGHT JOIN "Shop" T1 ON T1."id" = T0."shop"
```

</TabItem>
</Tabs>
````

## Cross join

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .crossJoin(CustomerTable)
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
, T1."id" c4
, T1."shop" c5
, T1."name" c6
, T1."spent" c7
FROM "Shop" T0
CROSS JOIN "Customer" T1
```

</TabItem>
</Tabs>
````

## Self-join with alias

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val alias = alias()

val row = ShopTable
    .innerJoin(ShopTable.as_(alias), alias[ShopTable.id] eq groceryStoreId)
    .where(ShopTable.id eq hardwareStoreId)
    .perform(db)
    .single()

check("Helen's Hardware" == row[ShopTable.name])
check("24 Hr Groceries" == row[alias[ShopTable.name]])
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
, T1."id" c4
, T1."name" c5
, T1."address" c6
, T1."established" c7
FROM "Shop" T0
INNER JOIN "Shop" T1 ON T1."id" = 2
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````
