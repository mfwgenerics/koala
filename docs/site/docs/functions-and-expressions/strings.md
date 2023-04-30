---
sidebar_position: 2
---

# String Operations

## Like

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val row = ShopTable
    .where(ShopTable.name like "%Hardware%")
    .where(ShopTable.name notLike "%Groceries%")
    .perform(db)
    .single()

check(hardwareStoreId == row.getValue(ShopTable.id))
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE (T0."name" LIKE '%Hardware%') AND (T0."name" NOT LIKE '%Groceries%')
```

</TabItem>
</Tabs>
````

## Upper and lower

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val (lowerName, upperName) = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(
        lower(ShopTable.name) as_ label(),
        upper(ShopTable.name) as_ label(),
    )
    .perform(db)
    .single()

check("helen's hardware" == lowerName)
check("HELEN'S HARDWARE" == upperName)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT LOWER(T0."name") c0
, UPPER(T0."name") c1
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````
