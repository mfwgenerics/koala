---
sidebar_position: 8
---

# Windows

## Window clause

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val shopsWindow = window() as_ all().partitionBy(ShopTable.id)

val totalSpent = sum(CustomerTable.spent).over(shopsWindow) as_ label()
val rank = rank().over(shopsWindow.orderBy(CustomerTable.spent.desc())) as_ label()

val rankings = ShopTable
    .innerJoin(CustomerTable, CustomerTable.shop eq ShopTable.id)
    .window(shopsWindow)
    .orderBy(ShopTable.name, rank)
    .select(
        ShopTable.name,
        CustomerTable.name,
        CustomerTable.spent,
        rank,
        totalSpent
    )
    .perform(db)
    .map { row ->
        "${row[CustomerTable.name]} #${row[rank]} at ${row[ShopTable.name]} " +
                "spent $${row[CustomerTable.spent]} of $${row[totalSpent]}"
    }
    .toList()

check(
    rankings.deepEquals(
        listOf(
            "Angela Abara #1 at 24 Hr Groceries spent $79.99 of $79.99",
            "Michael M. Michael #1 at Helen's Hardware spent $125.00 of $145.50",
            "Maria Robinson #2 at Helen's Hardware spent $20.50 of $145.50"
        )
    )
)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."name" c0
, T1."name" c1
, T1."spent" c2
, RANK() OVER (w0 ORDER BY T1."spent" DESC) c3
, SUM(T1."spent") OVER (w0) c4
FROM "Shop" T0
INNER JOIN "Customer" T1 ON T1."shop" = T0."id"
WINDOW w0 AS (PARTITION BY T0."id")
ORDER BY T0."name" ASC, c3 ASC
```

</TabItem>
</Tabs>
````
