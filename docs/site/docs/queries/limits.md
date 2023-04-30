---
sidebar_position: 7
---

# Limit and offset

## Limit

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val firstByName = ShopTable
    .orderBy(ShopTable.name)
    .limit(1)
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
ORDER BY T0."name" ASC
LIMIT 1
```

</TabItem>
</Tabs>
````

## Offset

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val thirdByName = ShopTable
    .orderBy(ShopTable.name)
    .offset(2)
    .limit(1)
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
ORDER BY T0."name" ASC
LIMIT 1
OFFSET 2
```

</TabItem>
</Tabs>
````

## Offset without limit

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .orderBy(ShopTable.name)
    .offset(2)
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
ORDER BY T0."name" ASC
OFFSET 2
```

</TabItem>
</Tabs>
````
