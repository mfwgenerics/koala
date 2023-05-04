---
sidebar_position: 4
---

# Where clauses

## Chaining wheres

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .where(ShopTable.name eq "Helen's Hardware")
    .perform(db)
    .single()
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE (T0."id" = 1) AND (T0."name" = 'Helen\'s Hardware')
```

</TabItem>
</Tabs>
````
