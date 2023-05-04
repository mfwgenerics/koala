---
sidebar_position: 5
---

# Grouping

## Group by

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val customerCount = label<Int>()

CustomerTable
    .groupBy(CustomerTable.shop)
    .select(CustomerTable.shop, count() as_ customerCount)
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."shop" c0
, COUNT(*) c1
FROM "Customer" T0
GROUP BY T0."shop"
```

</TabItem>
</Tabs>
````
