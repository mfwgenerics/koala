---
sidebar_position: 10
---

# Locks

## For update

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .where(ShopTable.id.inValues(groceryStoreId, hardwareStoreId))
    .forUpdate()
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE T0."id" IN (2, 1)
FOR UPDATE
```

</TabItem>
</Tabs>
````

# With
